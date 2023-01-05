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

import javax.validation.Validator;

import com.wl4g.infra.common.annotation.Todo;
import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifyMessage;

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
 * 注: 钉钉开放平台暂不支持获取群列表接口, 但可通过如下方式获取群ID(openConversationId)</br>
 * </br>
 * 方式1: 使用(创建群API)来获取群ID,
 * https://open.dingtalk.com/document/group/create-a-scene-group-v2</br>
 * </br>
 * 
 * 方式2: 配置订阅(群变更事件)来获取群ID,
 * https://open.dingtalk.com/document/group/business-integration-receiving</br>
 * </br>
 * </p>
 * 
 * <p>
 * step4: API 获取 accessToken:
 * https://open.dingtalk.com/document/orgapp-server/obtain-the-access_token-of-an-internal-app?spm=ding_open_doc.document.0.0.545715a7gp83NX
 * 
 * <pre>
 * curl -v \
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
 * curl -v \
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

}