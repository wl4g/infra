/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.notification.dingtalk;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.wl4g.infra.common.annotation.Todo;
import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifyMessage;
import com.wl4g.infra.common.notification.dingtalk.internal.DingCallbackCrypto;
import com.wl4g.infra.common.remoting.HttpRequestEntity;
import com.wl4g.infra.common.remoting.HttpResponseEntity;
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
 * 方式1(无审批), (告警机器人)企业内部应用配置步骤:
 * <p>
 * step1: 创建企业内部应用: 应用开发->企业内部开发->钉钉应用(H5微应用):
 * https://open-dev.dingtalk.com/fe/app#/corp/app
 * </p>
 * 
 * <p>
 * step2: 为企业内部应用启用推送(机器人) + 复制机器人ID:
 * 应用开发->企业内部开发->钉钉应用->[myapp]->应用功能->消息推送->推送方式->机器人->(拷贝RobotCode):
 * https://open-dev.dingtalk.com/fe/app#/appMgr/inner/h5/2346940718/13
 * </p>
 * 
 * <p>
 * step3: 获取群ID(openConversationId):</br>
 * </br>
 * 注: 钉钉开放平台(服务端API)暂不支持获取群列表API,
 * 但可通过(前端API选择会话chatId)，或使用如下几种方式获取群ID(openConversationId)</br>
 * </br>
 * 
 * 方式1: 创建群时 API 返回群ID,
 * https://open.dingtalk.com/document/group/create-a-scene-group-v2</br>
 * </br>
 * 
 * </br>
 * 方式2: 通过 chatId 获取群ID (官方文档提示chatId即将过时后续不再使用, 推荐使用OpenConversationId),
 * https://open.dingtalk.com/document/orgapp-server/obtain-group-openconversationid</br>
 * 
 * <pre>
 * curl -v -XPOST \
 *   -H "Host: api.dingtalk.com" \
 *   -H "Content-Type: application/json" \
 *   https://api.dingtalk.com/v1.0/im/chat/{chatId}/convertToOpenConversationId
 * </pre>
 * 
 * </br>
 * 
 * 方式3: 配置订阅(群变更事件)来获取群ID,
 * https://open.dingtalk.com/document/group/business-integration-receiving</br>
 * </br>
 * </p>
 * 
 * <p>
 * step4: API 获取 accessToken:
 * https://open.dingtalk.com/document/orgapp-server/obtain-the-access_token-of-an-internal-app?spm=ding_open_doc.document.0.0.545715a7gp83NX
 * 
 * <pre>
 * curl -v -XPOST \
 *   -H "Host: api.dingtalk.com" \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "appKey": "you_app_key",
 *     "appSecret": "you_app_secret"
 *   }' https://api.dingtalk.com/v1.0/oauth2/accessToken
 * </pre>
 * </p>
 * 
 * <p>
 * step5: API (机器人)发送消息:
 * https://open.dingtalk.com/document/group/the-robot-sends-a-group-message
 * 
 * <pre>
 * curl -v -XPOST \
 * -H "Host: api.dingtalk.com" \
 * -H "x-acs-dingtalk-access-token: 11e5fafc89653a87b5c8f55a131d84f6" \
 * -H "Content-Type: application/json" \
 * -d '{
 *   "msgParam": "abc",
 *   "msgKey": "abc",
 *   "openConversationId": "",
 *   "robotCode": "ding7edru85pcbhxmv24"
 * }' https://api.dingtalk.com/v1.0/robot/groupMessages/send
 * </pre>
 * </p>
 * 
 * 方式二(需审批), (告警机器人)场景群配置步骤:
 * <p>
 * 开放能力->场景群->群模版: https://open-dev.dingtalk.com/fe/im#/group/list
 * </p>
 * 
 * <p>
 * 开放能力-场景群-机器人: https://open-dev.dingtalk.com/fe/im#/robot/list
 * </p>
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 */
@CustomLog
public class DingtalkMessageNotifier extends AbstractMessageNotifier<DingtalkNotifyProperties> {

    public DingtalkMessageNotifier(DingtalkNotifyProperties config, Validator validator) {
        super(config, validator);
    }

    @Override
    public NotifierKind kind() {
        return NotifierKind.DINGTALK;
    }

    @Override
    public void send(GenericNotifyMessage message) {
        // TODO

    }

    @Todo
    @Override
    public <R> R sendForReply(GenericNotifyMessage message) {
        throw new UnsupportedOperationException();
    }

    public static AccessTokenResult getAccessToken(final @NotNull AccessToken request) {
        return doRequest(ACCESS_TOKEN_URI, HttpMethod.POST, null, request, AccessTokenResult.class, true, false);
    }

    public static GetUserIdByMobileResult getUserIdByMobile(final @NotBlank String accessToken, final @NotNull String mobile) {
        return doRequest(format(GET_USERID_BY_MOBILE_URI, accessToken, mobile), HttpMethod.GET, null, null,
                GetUserIdByMobileResult.class, false, false);
    }

    public static CreateSceneGroupV2Result createSceneGroupV2(
            final @NotBlank String accessToken,
            final @NotNull CreateSceneGroupV2 request) {
        return doRequest(format(CREATE_GROUP_V2_URI, accessToken), HttpMethod.POST, null, request, CreateSceneGroupV2Result.class,
                true, false);
    }

    public static RobotGroupMessagesSendResult sendRobotGroupMessages(
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
    public static Map<String, String> processCallback(
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

    static <T, R> R doRequest(
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
        final HttpResponseEntity<R> result = new RestClient().exchange(entity, resultClass);
        return result.getBody();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class BaseResult {
        private @JsonProperty("request_id") String requestId;
        private Integer errcode;
        private String errmsg;
        private Boolean success;
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
        private @NotBlank String appKey;
        private @NotBlank String appSecret;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class AccessTokenResult extends BaseResult {
        private @NotNull Integer expireIn;
        private @NotBlank String accessToken;
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
        private String userid;
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
        private @NotBlank @Default String title = "测试群-" + currentTimeMillis();
        private @NotBlank String template_id;
        private @NotBlank String owner_user_id;
        private @NotBlank String user_ids;
        private @NotBlank String subadmin_ids;
        private String uuid;
        private String icon;
        private @Default String mention_all_authority = "0";
        private @Default String show_history_type = "0";
        private @Default String validation_type = "0";
        private @Default String searchable = "0";
        private @Default String chat_banned_type = "0";
        private @Default String management_type = "0";
        private @Default String only_admin_can_ding = "0";
        private @Default String all_members_can_create_mcs_conf = "1";
        private @Default String all_members_can_create_calendar = "0";
        private @Default String group_email_disabled = "0";
        private @Default String only_admin_can_set_msg_top = "0";
        private @Default String add_friend_forbidden = "0";
        private @Default String group_live_switch = "1";
        private @Default String members_to_admin_chat = "0";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class CreateSceneGroupV2Result extends BaseResult {
        private CreateSceneGroupV2ResultContent result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class CreateSceneGroupV2ResultContent {
        private @JsonProperty("chat_id") String chatId;
        private @JsonProperty("open_conversation_id") String openConversationId;
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
        private @NotBlank MsgKeyType msgKey;
        private @NotBlank String msgParam;
        private @NotBlank String openConversationId;
        private @NotBlank String robotCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class RobotGroupMessagesSendResult extends BaseResult {
        private String processQueryKey;
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
        //private @NotBlank @EnumValue(enumCls = MsgKeyType.class) MsgKeyType type;
        // @formatter:on
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleTextMsgParam extends MsgParamBase {
        private @NotBlank String content;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleMarkdownMsgParam extends MsgParamBase {
        private @NotBlank String title;
        private @NotBlank String text;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleImageMsgParam extends MsgParamBase {
        private @NotBlank String photoURL;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SimpleLinkMsgParam extends MsgParamBase {
        private @NotBlank String title;
        private @NotBlank String text;
        private @NotBlank String picUrl;
        private @NotBlank String messageUrl;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCardParam extends MsgParamBase {
        private @NotBlank String title;
        private @NotBlank String text;
        private @NotBlank String singleTitle;
        private @NotBlank String singleURL;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard2Param extends MsgParamBase {
        private @NotBlank String text;
        private @NotBlank String title;
        private @NotBlank String actionTitle1;
        private @NotBlank String actionURL1;
        private @NotBlank String actionTitle2;
        private @NotBlank String actionURL2;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard3Param extends MsgParamBase {
        private @NotBlank String text;
        private @NotBlank String title;
        private @NotBlank String actionTitle1;
        private @NotBlank String actionURL1;
        private @NotBlank String actionTitle2;
        private @NotBlank String actionURL2;
        private @NotBlank String actionTitle3;
        private @NotBlank String actionURL3;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard4Param extends MsgParamBase {
        private @NotBlank String text;
        private @NotBlank String title;
        private @NotBlank String actionTitle1;
        private @NotBlank String actionURL1;
        private @NotBlank String actionTitle2;
        private @NotBlank String actionURL2;
        private @NotBlank String actionTitle3;
        private @NotBlank String actionURL3;
        private @NotBlank String actionTitle4;
        private @NotBlank String actionURL4;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard5Param extends MsgParamBase {
        private @NotBlank String text;
        private @NotBlank String title;
        private @NotBlank String actionTitle1;
        private @NotBlank String actionURL1;
        private @NotBlank String actionTitle2;
        private @NotBlank String actionURL2;
        private @NotBlank String actionTitle3;
        private @NotBlank String actionURL3;
        private @NotBlank String actionTitle4;
        private @NotBlank String actionURL4;
        private @NotBlank String actionTitle5;
        private @NotBlank String actionURL5;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @ToString(callSuper = true)
    public static class SampleActionCard6Param extends MsgParamBase {
        private @NotBlank String text;
        private @NotBlank String title;
        private @NotBlank String buttonTitle1;
        private @NotBlank String buttonUrl1;
        private @NotBlank String buttonTitle2;
        private @NotBlank String buttonUrl2;
    }

    public static final String ACCESS_TOKEN_URI = "https://api.dingtalk.com/v1.0/oauth2/accessToken";
    public static final String GET_USERID_BY_MOBILE_URI = "https://oapi.dingtalk.com/user/get_by_mobile?access_token=%s&mobile=%s";
    public static final String CREATE_GROUP_V2_URI = "https://oapi.dingtalk.com/topapi/im/chat/scenegroup/create?access_token=%s";
    public static final String SEND_GROUP_MESSAGE_URI = "https://api.dingtalk.com/v1.0/robot/groupMessages/send";

}