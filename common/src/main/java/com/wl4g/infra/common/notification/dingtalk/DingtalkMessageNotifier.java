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
import static com.wl4g.infra.common.lang.Assert2.notEmptyOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifierParam;
import com.wl4g.infra.common.notification.dingtalk.internal.DingCallbackCrypto;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.CreateSceneGroupV2;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.CreateSceneGroupV2Result;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.MsgKeyType;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.RobotGroupMessagesSend;

/**
 * 从零开始配置和测试一个钉钉告警机器人的步骤如下:</br>
 * 
 * 1) 配置<企业内部应用+机器人>步骤:
 * <p>
 * 1.1) [控制台操作]创建企业内部应用: 应用开发->企业内部开发->钉钉应用(H5微应用):
 * 参见:https://open-dev.dingtalk.com/fe/app#/corp/app
 * </p>
 * 
 * <p>
 * 1.2) [控制台操作]启用推送机器人并拷贝机器人ID:
 * 应用开发->企业内部开发->钉钉应用->[myapp]->应用功能->消息推送->推送方式->机器人->(拷贝RobotCode):
 * 参见:https://open-dev.dingtalk.com/fe/app#/appMgr/inner/h5/2346940718/13
 * </p>
 * 
 * <p>
 * 1.3) [API操作]获取群ID(openConversationId)(不推荐, 如果无chatId则无法使用,无意义):</br>
 * 注:钉钉开放平台(服务端API)暂不支持获取群列表的API,但可通过(前端API)选择会话chatId，或者使用如下几种方式获取群ID(openConversationId)</br>
 * 1.3.1) 创建群时API返回群ID
 * 参见:https://open.dingtalk.com/document/group/create-a-scene-group-v2</br>
 * 1.3.2) 通过chatId获取群ID(官方文档提示chatId即将过时后续不再使用,推荐使用OpenConversationId)
 * 参见:https://open.dingtalk.com/document/orgapp-server/obtain-group-openconversationid</br>
 * 
 * <pre>
 * curl -v -XPOST \
 *   -H "Host: api.dingtalk.com" \
 *   -H "Content-Type: application/json" \
 *   https://api.dingtalk.com/v1.0/im/chat/{chatId}/convertToOpenConversationId
 * </pre>
 * 
 * 1.3.3) 配置订阅(群变更事件)来获取群ID,
 * 参见:https://open.dingtalk.com/document/group/business-integration-receiving</br>
 * </p>
 * 
 * <p>
 * 1.4) [API操作]获取accessToken
 * 参见:https://open.dingtalk.com/document/orgapp-server/obtain-the-access_token-of-an-internal-app?spm=ding_open_doc.document.0.0.545715a7gp83NX
 * 
 * <pre>
 * curl -v -XPOST \
 *   -H "Host: api.dingtalk.com" \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "appKey": "dingbhyrzjxx6qjhjcdr",
 *     "appSecret": "<you_app_secret>"
 *   }' https://api.dingtalk.com/v1.0/oauth2/accessToken
 * 
 * {"expireIn":7200,"accessToken":"03ac6a7029ab339387b301e29e7ec094"}
 * </pre>
 * </p>
 * 
 * <p>
 * 1.5) [API操作]根据手机号获取userId
 * 参见:https://open.dingtalk.com/document/orgapp-server/retrieve-userid-from-mobile-phone-number?spm=ding_open_doc.document.0.0.60dc722fpc6eP6#topic-1936808
 * 
 * <pre>
 * curl -v -XGET 'https://oapi.dingtalk.com/user/get_by_mobile?access_token=03ac6a7029ab339387b301e29e7ec094&mobile=180074xxxxx'
 * 
 * {"errcode":0,"errmsg":"ok","userid":"6165471647114842627"}
 * </pre>
 * </p>
 * 
 * 2) 配置<场景群+机器人>步骤:
 * <p>
 * 2.1) [控制台操作]创建群模版并拷贝templateId: 开放能力->场景群->群模版:
 * 参见:https://open-dev.dingtalk.com/fe/im#/group/list
 * </p>
 * 
 * <p>
 * 2.2) [API操作]使用群模版创建会话群
 * 参见:https://open.dingtalk.com/document/group/create-a-scene-group-v2
 * 
 * <pre>
 * curl -v -XPOST \
 *    -H "Content-Type: application/json" \
 *    -d '{
 *         "title":"测试群01",
 *         "templateId":"4ba6847f-b9b0-42ca-96ea-22c4ed8a3fbd",
 *         "ownerUserId":"6165471647114842627",
 *         "userIds":"6165471647114842627",
 *         "subadmin_ids":"6165471647114842627",
 *         "uuid":"axcf*-*****-*****-23da*",
 *         "icon":"@lADOADma*****QKA",
 *         "mention_all_authority":0,
 *         "show_history_type":0,
 *         "validation_type":0,
 *         "searchable":0,
 *         "chat_banned_type":0,
 *         "management_type":0,    
 *         "only_admin_can_ding":0,
 *         "all_members_can_create_mcs_conf":1,
 *         "all_members_can_create_calendar":0,
 *         "group_email_disabled":0,
 *         "only_admin_can_set_msg_top":0, 
 *         "add_friend_forbidden":0,
 *         "group_live_switch":1,
 *         "members_to_admin_chat":0
 *   }' 'https://oapi.dingtalk.com/topapi/im/chat/scenegroup/create?access_token=03ac6a7029ab339387b301e29e7ec094'
 * 
 * {"errcode":0,"errmsg":"ok","result":{"chat_id":"chat7b43308b68ec835f9ba9a5e440a4cce6","open_conversation_id":"cide3M7a7Ldu5TG9+8BH75JWA=="},"success":true,"request_id":"15rkz033zqmb8"}
 * </pre>
 * </p>
 * 
 * <p>
 * 2.3: [API操作]发送机器人群消息
 * 参见:https://open.dingtalk.com/document/group/the-robot-sends-a-group-message
 * 
 * <pre>
 * curl -v -XPOST \
 * -H "Host: api.dingtalk.com" \
 * -H "x-acs-dingtalk-access-token: 03ac6a7029ab339387b301e29e7ec094" \
 * -H "Content-Type: application/json" \
 * -d '{
 *   "msgParam": "{\"title\":\"(故障演练)异常告警\",\"text\":\"- 告警时间: 2023-01-01 01:01:01\n- 集群环境: production\n- 节点 IP: 10.0.0.112\n- 诊断消息: <font color
='#ff0000'>send_kafka_fail_rate > 30%</font>\n- **[更多指标](http://grafana.example.com/123)**\"}",
 *   "msgKey": "sampleMarkdown",
 *   "openConversationId": "cide3M7a7Ldu5TG9+8BH75JWA==",
 *   "robotCode": "dingbhyrzjxx6qjhjcdr"
 * }' https://api.dingtalk.com/v1.0/robot/groupMessages/send
 * 
 * {"processQueryKey":"VRg4vX1aqLmdUPuSa53RxLWprzwGoMHeMBV5VTm1AHQ="}
 * </pre>
 * </p>
 * 
 * <p>
 * <b>注:</b>
 * <ul>
 * <li>测试发现, 仅通过API+群模版创建的会话群才支持API机器人推送???(待验证), 且发现无法删除通过API创建群时关联的<场景群-机器人>,
 * 但可添加和删除手动添加的<企业内部应用-机器人>, 关于这两种机器人的区别有待详细验证(文档无详解).</li>
 * 
 * <li>解密事件消息时,corpId对于<企业内部应用>其实就是appKey,但对于<第三方应用>则是suiteKey.参见:{@link DingCallbackCrypto}</li>
 * </ul>
 * </p>
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v3.0.0
 */
public class DingtalkMessageNotifier extends AbstractMessageNotifier<DingtalkNotifierProperties> {

    public DingtalkMessageNotifier(DingtalkNotifierProperties config, Validator validator) {
        super(config, validator);
    }

    @Override
    public NotifierKind kind() {
        return NotifierKind.DINGTALK;
    }

    @Override
    public Object send(GenericNotifierParam msg) {
        final String accessToken = msg.getParameterAsString(KEY_ACCESS_TOKEN, "");
        final String msgKey = msg.getParameterAsString(KEY_MSG_KEY, MsgKeyType.sampleMarkdown.name());
        final String msgParam = msg.getParameterAsString(KEY_MSG_PARAM, "");
        final String robotCode = msg.getParameterAsString(KEY_ROBOT_CODE, "");

        hasTextOf(accessToken, "acccessToken");
        hasTextOf(msgKey, "msgKey");
        hasTextOf(msgParam, "msgParam");
        hasTextOf(robotCode, "robotCode");
        notEmptyOf(msg.getToObjects(), "openConversationId");
        final String openConversationId = msg.getToObjects().get(0);
        hasTextOf(openConversationId, "openConversationId");

        final RobotGroupMessagesSend request = RobotGroupMessagesSend.builder()
                .msgKey(MsgKeyType.valueOf(msgKey))
                .msgParam(msgParam)
                .openConversationId(openConversationId)
                .robotCode(robotCode)
                .build();
        return DingtalkAPI.getInstance().sendRobotGroupMessages(accessToken, request);
    }

    public CreateSceneGroupV2Result createSceneGroupV2(
            final @NotBlank String accessToken,
            final @NotNull CreateSceneGroupV2 request) {
        hasTextOf(accessToken, "accessToken");
        notNullOf(request, "request");
        return DingtalkAPI.getInstance().createSceneGroupV2(accessToken, request);
    }

    public static final String KEY_ACCESS_TOKEN = "accessToken";
    public static final String KEY_MSG_KEY = "msgKey";
    public static final String KEY_MSG_PARAM = "msgParam";
    public static final String KEY_OPEN_CONVERSATION_ID = "openConversationId";
    public static final String KEY_ROBOT_CODE = "robotCode";
}