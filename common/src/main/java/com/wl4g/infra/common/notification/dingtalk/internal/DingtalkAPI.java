/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong James Wong <jameswong1376@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.notification.dingtalk.internal;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getBooleanProperty;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getIntProperty;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.wl4g.infra.common.remoting.HttpRequestEntity;
import com.wl4g.infra.common.remoting.HttpResponseEntity;
import com.wl4g.infra.common.remoting.Netty4ClientHttpRequestFactory;
import com.wl4g.infra.common.remoting.RestClient;
import com.wl4g.infra.common.remoting.standard.HttpHeaders;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Builder.Default;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link DingtalkAPI}
 * 
 * @author James Wong
 * @version 2023-01-06
 * @since v3.0.0
 */
@CustomLog
public class DingtalkAPI {
    private final RestClient restClient;

    public DingtalkAPI() {
        final String configPrefix = DingtalkAPI.class.getSimpleName().toLowerCase();
        final boolean debug = getBooleanProperty(configPrefix + ".debug", false);
        final int connectTimeoutMs = getIntProperty(configPrefix + ".connectTimeout", 0);
        final int readTimeoutMs = getIntProperty(configPrefix + ".readTimeout", 0);
        final int maxResponseSize = getIntProperty(configPrefix + ".maxResponseSize", 0);
        this.restClient = new RestClient(
                new Netty4ClientHttpRequestFactory(debug, connectTimeoutMs, readTimeoutMs, maxResponseSize));
    }

    public DingtalkAPI(final @NotNull RestClient restClient) {
        this.restClient = notNullOf(restClient, "restClient");
    }

    public AccessTokenResult getAccessToken(final @NotNull AccessToken request) {
        return doRequest(ACCESS_TOKEN_URI, HttpMethod.POST, null, request, AccessTokenResult.class, true, false);
    }

    public GetUserIdByMobileResult getUserIdByMobile(final @NotBlank String accessToken, final @NotNull String mobile) {
        return doRequest(format(GET_USERID_BY_MOBILE_URI, accessToken, mobile), HttpMethod.GET, null, null,
                GetUserIdByMobileResult.class, false, false);
    }

    public CreateSceneGroupV2Result createSceneGroupV2(
            final @NotBlank String accessToken,
            final @NotNull CreateSceneGroupV2 request) {
        return doRequest(format(CREATE_GROUP_V2_URI, accessToken), HttpMethod.POST, null, request, CreateSceneGroupV2Result.class,
                true, false);
    }

    public RobotGroupMessagesSendResult sendRobotGroupMessages(
            final @NotBlank String accessToken,
            final @NotNull RobotGroupMessagesSend request) {
        return doRequest(SEND_GROUP_MESSAGE_URI, HttpMethod.POST, accessToken, request, RobotGroupMessagesSendResult.class, true,
                true);
    }

    /**
     * Validation and processing the subscribed group event callback request.
     * 
     * @param aesToken
     * @param aesKey
     * @param corpId
     *            1. 开发者后台配置的订阅事件为应用级事件推送，此时OWNER_KEY为应用的APP_KEY. </br>
     *            2. 调用订阅事件接口订阅的事件为企业级事件推送,
     *            此时OWNER_KEY为：企业的appkey（企业内部应用）或SUITE_KEY（三方应用）
     * @param signature
     * @param timestamp
     * @param nonce
     * @param bodyJson
     * @return
     * @see https://open.dingtalk.com/document/orgapp-server/configure-event-subcription
     * @see https://github.com/open-dingtalk/dingtalk-callback-Crypto?spm=ding_open_doc.document.0.0.6ecd7008UVMrZc
     */
    public Map<String, String> processCallback(
            final @NotBlank String aesToken,
            final @NotBlank String aesKey,
            final @NotBlank String corpId,
            final @NotBlank String signature,
            final @NotBlank String timestamp,
            final @NotBlank String nonce,
            final @NotBlank String bodyJson,
            final @NotNull Consumer<JsonNode> process) {
        hasTextOf(aesToken, "aesToken");
        hasTextOf(aesKey, "aesKey");
        hasTextOf(corpId, "corpId");
        hasTextOf(timestamp, "timestamp");
        hasTextOf(nonce, "nonce");
        hasTextOf(bodyJson, "bodyJson");
        notNullOf(process, "process");

        try {
            final JsonNode json = parseToNode(bodyJson);
            final String encryptMsg = json.requiredAt("/encrypt").asText();

            final DingCallbackCrypto crypto = new DingCallbackCrypto(aesToken, aesKey, corpId);
            final String decryptMsg = crypto.getDecryptMsg(signature, timestamp, nonce, encryptMsg);

            // 提取回调请求的明文事件JSON数据
            final JsonNode eventJson = parseToNode(decryptMsg);
            log.debug("Received event: {}", eventJson.requiredAt("/EventType").asText());

            // 处理事件消息
            process.accept(eventJson);

            // 返回success的加密数据
            return crypto.getEncryptedMap("success");
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validation subscribed callback request.", e);
        }
    }

    <T, R> R doRequest(
            final @NotBlank String url,
            final @NotNull HttpMethod method,
            final @Nullable String accessToken,
            final @Nullable T request,
            final @NotNull Class<R> resultClass,
            final boolean addContentJson,
            final boolean addAccessToken) {
        notNullOf(method, "method");
        notNullOf(resultClass, "resultClass");
        final HttpHeaders headers = new HttpHeaders();
        if (addContentJson) {
            headers.add("Content-Type", "application/json");
        }
        if (addAccessToken) {
            headers.add("x-acs-dingtalk-access-token", accessToken);
        }
        final HttpRequestEntity<Object> entity = new HttpRequestEntity<>(request, headers, method, URI.create(url));
        final HttpResponseEntity<R> result = restClient.exchange(entity, resultClass);
        return result.getBody();
    }

    public static DingtalkAPI getInstance() {
        if (isNull(DEFAULT)) {
            synchronized (DingtalkAPI.class) {
                if (isNull(DEFAULT)) {
                    DEFAULT = new DingtalkAPI();
                }
            }
        }
        return DEFAULT;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class BaseResult {
        String requestId;
        Integer errcode;
        String errmsg;
        Boolean success;
    }

    /**
     * 1. API 获取 accessToken:
     * https://open.dingtalk.com/document/orgapp-server/obtain-the-access_token-of-an-internal-app?spm=ding_open_doc.document.0.0.545715a7gp83NX</br>
     * 2. 要求权限(默认开通): 调用企业API基础权限/(qyapi_base)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @ToString(callSuper = true)
    public static class AccessToken {
        @NotBlank
        String appKey;
        @NotBlank
        String appSecret;

        public AccessToken validate() {
            hasTextOf(getAppKey(), "appKey");
            hasTextOf(getAppSecret(), "appSecret");
            return this;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class AccessTokenResult extends BaseResult {
        @NotNull
        Integer expireIn;
        @NotBlank
        String accessToken;
    }

    /**
     * 1. API 根据手机号获取 userId:
     * https://open.dingtalk.com/document/orgapp-server/retrieve-userid-from-mobile-phone-number?spm=ding_open_doc.document.0.0.60dc722fpc6eP6#topic-1936808</br>
     * 2. 要求权限: 根据手机号姓名获取成员信息的接口访问权限(qyapi_get_member_by_mobile)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class GetUserIdByMobileResult extends BaseResult {
        String userid;
    }

    /**
     * 1. API 创建场景群:
     * https://open.dingtalk.com/document/group/create-a-scene-group-v2</br>
     * 2. 要求权限: chat相关接口的管理权限(qyapi_chat_manage)
     */
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class CreateSceneGroupV2 {
        @NotBlank
        @Default
        String title = "测试群-" + currentTimeMillis();

        @NotBlank
        @JsonProperty("template_id")
        String templateId;

        @NotBlank
        @JsonProperty("owner_user_id")
        String ownerUserId;

        @NotBlank
        @JsonProperty("user_ids")
        String userIds;

        @JsonProperty("subadmin_ids")
        @NotBlank
        String subadminIds;

        String uuid;

        String icon;

        @JsonProperty("mention_all_authority")
        @Default
        String mentionAllAuthority = "0";

        @JsonProperty("show_history_type")
        @Default
        String showHistoryType = "0";

        @JsonProperty("validation_type")
        @Default
        String validationType = "0";

        @Default
        String searchable = "0";

        @JsonProperty("chat_banned_type")
        @Default
        String chatVannedType = "0";

        @JsonProperty("management_type")
        @Default
        String managementType = "0";

        @JsonProperty("only_admin_can_ding")
        @Default
        String onlyAdminCanDing = "0";

        @JsonProperty("all_members_can_create_mcs_conf")
        @Default
        String allMembersCanCreateMcsConf = "1";

        @JsonProperty("all_members_can_create_calendar")
        @Default
        String allMembersCanCreateCalendar = "0";

        @JsonProperty("group_email_disabled")
        @Default
        String groupEmailDisabled = "0";

        @JsonProperty("only_admin_can_set_msg_top")
        @Default
        String onlyAdminCanSetMsgTop = "0";

        @JsonProperty("add_friend_forbidden")
        @Default
        String addFriendForbidden = "0";

        @JsonProperty("group_live_switch")
        @Default
        String groupLiveSwitch = "1";

        @JsonProperty("members_to_admin_chat")
        @Default
        String membersToAdminChat = "0";

        public CreateSceneGroupV2 validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getTemplateId(), "templateId");
            hasTextOf(getOwnerUserId(), "ownerUserId");
            hasTextOf(getUserIds(), "userIds");
            hasTextOf(getSubadminIds(), "subadminIds");
            return this;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class CreateSceneGroupV2Result extends BaseResult {
        CreateSceneGroupV2ResultContent result;

        @JsonProperty("request_id")
        public String getRequestId() {
            return this.requestId;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class CreateSceneGroupV2ResultContent {
        @JsonProperty("chat_id")
        String chatId;
        @JsonProperty("open_conversation_id")
        String openConversationId;
    }

    /**
     * 1. API 群发消息(企业内部开发-机器人):
     * https://open.dingtalk.com/document/group/the-robot-sends-a-group-message</br>
     * 2. 要求权限: 企业内机器人发送消息权限(qyapi_robot_sendmsg)
     */
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class RobotGroupMessagesSend {
        @NotBlank
        MsgKeyType msgKey;
        @NotBlank
        String msgParam;
        @NotBlank
        String openConversationId;
        @NotBlank
        String robotCode;

        public RobotGroupMessagesSend validate() {
            notNullOf(getMsgKey(), "msgKey");
            hasTextOf(getMsgParam(), "msgParam");
            hasTextOf(getOpenConversationId(), "openConversationId");
            hasTextOf(getRobotCode(), "robotCode");
            return this;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class RobotGroupMessagesSendResult extends BaseResult {
        String processQueryKey;
    }

    /**
     * 参见(发送内容格式定义):
     * https://open.dingtalk.com/document/group/message-types-and-data-format
     */
    public static enum MsgKeyType {
        sampleText,

        sampleMarkdown,

        sampleImageMsg,

        sampleLink,

        sampleActionCard,

        sampleActionCard2,

        sampleActionCard3,

        sampleActionCard4,

        sampleActionCard5,

        sampleActionCard6
    }

    // @formatter:off
    //@Schema(oneOf = { SimpleTextMsgParam.class, SimpleMarkdownMsgParam.class, SimpleImageMsgParam.class, SimpleLinkMsgParam.class,
    //        SampleActionCardParam.class, SampleActionCard2Param.class, SampleActionCard3Param.class, SampleActionCard4Param.class,
    //        SampleActionCard5Param.class, SampleActionCard6Param.class }, discriminatorProperty = "@type")
    //@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", visible = true)
    //@JsonSubTypes({ @Type(value = SimpleTextMsgParam.class, name = "sampleText"),
    //        @Type(value = SimpleMarkdownMsgParam.class, name = "sampleMarkdown"),
    //        @Type(value = SimpleImageMsgParam.class, name = "sampleImageMsg"),
    //        @Type(value = SimpleLinkMsgParam.class, name = "sampleLink"),
    //        @Type(value = SampleActionCardParam.class, name = "sampleActionCard"),
    //        @Type(value = SampleActionCard2Param.class, name = "sampleActionCard2"),
    //        @Type(value = SampleActionCard3Param.class, name = "sampleActionCard3"),
    //        @Type(value = SampleActionCard4Param.class, name = "sampleActionCard4"),
    //        @Type(value = SampleActionCard5Param.class, name = "sampleActionCard5"),
    //        @Type(value = SampleActionCard6Param.class, name = "sampleActionCard6") })
    // @formatter:on
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static abstract class MsgParamBase {
        // @formatter:off
        //@Schema(name = "@type", implementation = MsgKeyType.class)
        //@JsonProperty(value = "@type")
        //@NotBlank @EnumValue(enumCls = MsgKeyType.class) MsgKeyType type;
        // @formatter:on
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleTextMsgParam extends MsgParamBase {
        @NotBlank
        String content;

        public SimpleTextMsgParam validate() {
            hasTextOf(getContent(), "content");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleMarkdownMsgParam extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;

        public SimpleMarkdownMsgParam validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleImageMsgParam extends MsgParamBase {
        @NotBlank
        String photoURL;

        public SimpleImageMsgParam validate() {
            hasTextOf(getPhotoURL(), "photoURL");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleLinkMsgParam extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String picUrl;
        @NotBlank
        String messageUrl;

        public SimpleLinkMsgParam validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getPicUrl(), "picUrl");
            hasTextOf(getMessageUrl(), "messageUrl");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCardParam extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String singleTitle;
        @NotBlank
        String singleURL;

        public SampleActionCardParam validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getSingleTitle(), "singleTitle");
            hasTextOf(getSingleURL(), "singleURL");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard2Param extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String actionTitle1;
        @NotBlank
        String actionURL1;
        @NotBlank
        String actionTitle2;
        @NotBlank
        String actionURL2;

        public SampleActionCard2Param validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getActionTitle1(), "actionTitle1");
            hasTextOf(getActionURL1(), "actionURL1");
            hasTextOf(getActionTitle2(), "actionTitle2");
            hasTextOf(getActionURL2(), "actionURL2");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard3Param extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String actionTitle1;
        @NotBlank
        String actionURL1;
        @NotBlank
        String actionTitle2;
        @NotBlank
        String actionURL2;
        @NotBlank
        String actionTitle3;
        @NotBlank
        String actionURL3;

        public SampleActionCard3Param validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getActionTitle1(), "actionTitle1");
            hasTextOf(getActionURL1(), "actionURL1");
            hasTextOf(getActionTitle2(), "actionTitle2");
            hasTextOf(getActionURL2(), "actionURL2");
            hasTextOf(getActionTitle3(), "actionTitle3");
            hasTextOf(getActionURL3(), "actionURL3");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard4Param extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String actionTitle1;
        @NotBlank
        String actionURL1;
        @NotBlank
        String actionTitle2;
        @NotBlank
        String actionURL2;
        @NotBlank
        String actionTitle3;
        @NotBlank
        String actionURL3;
        @NotBlank
        String actionTitle4;
        @NotBlank
        String actionURL4;

        public SampleActionCard4Param validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getActionTitle1(), "actionTitle1");
            hasTextOf(getActionURL1(), "actionURL1");
            hasTextOf(getActionTitle2(), "actionTitle2");
            hasTextOf(getActionURL2(), "actionURL2");
            hasTextOf(getActionTitle3(), "actionTitle3");
            hasTextOf(getActionURL3(), "actionURL3");
            hasTextOf(getActionTitle4(), "actionTitle4");
            hasTextOf(getActionURL4(), "actionURL4");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard5Param extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String actionTitle1;
        @NotBlank
        String actionURL1;
        @NotBlank
        String actionTitle2;
        @NotBlank
        String actionURL2;
        @NotBlank
        String actionTitle3;
        @NotBlank
        String actionURL3;
        @NotBlank
        String actionTitle4;
        @NotBlank
        String actionURL4;
        @NotBlank
        String actionTitle5;
        @NotBlank
        String actionURL5;

        public SampleActionCard5Param validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getActionTitle1(), "actionTitle1");
            hasTextOf(getActionURL1(), "actionURL1");
            hasTextOf(getActionTitle2(), "actionTitle2");
            hasTextOf(getActionURL2(), "actionURL2");
            hasTextOf(getActionTitle3(), "actionTitle3");
            hasTextOf(getActionURL3(), "actionURL3");
            hasTextOf(getActionTitle4(), "actionTitle4");
            hasTextOf(getActionURL4(), "actionURL4");
            hasTextOf(getActionTitle5(), "actionTitle5");
            hasTextOf(getActionURL5(), "actionURL5");
            return this;
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard6Param extends MsgParamBase {
        @NotBlank
        String title;
        @NotBlank
        String text;
        @NotBlank
        String buttonTitle1;
        @NotBlank
        String buttonUrl1;
        @NotBlank
        String buttonTitle2;
        @NotBlank
        String buttonUrl2;

        public SampleActionCard6Param validate() {
            hasTextOf(getTitle(), "title");
            hasTextOf(getText(), "text");
            hasTextOf(getButtonTitle1(), "buttonTitle1");
            hasTextOf(getButtonUrl1(), "buttonUrl1");
            hasTextOf(getButtonTitle2(), "buttonTitle2");
            hasTextOf(getButtonUrl2(), "buttonUrl2");
            return this;
        }
    }

    private static DingtalkAPI DEFAULT;
    static final String ACCESS_TOKEN_URI = "https://api.dingtalk.com/v1.0/oauth2/accessToken";
    static final String GET_USERID_BY_MOBILE_URI = "https://oapi.dingtalk.com/user/get_by_mobile?access_token=%s&mobile=%s";
    static final String CREATE_GROUP_V2_URI = "https://oapi.dingtalk.com/topapi/im/chat/scenegroup/create?access_token=%s";
    static final String SEND_GROUP_MESSAGE_URI = "https://api.dingtalk.com/v1.0/robot/groupMessages/send";
}
