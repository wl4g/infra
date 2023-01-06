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
package com.wl4g.infra.common.notification;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import org.junit.Test;

import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.AccessToken;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.AccessTokenResult;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.CreateSceneGroupV2;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.CreateSceneGroupV2Result;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.GetUserIdByMobileResult;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.MsgKeyType;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.RobotGroupMessagesSend;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.RobotGroupMessagesSendResult;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier.SampleActionCard6Param;

/**
 * {@link DingtalkMessageNotifierTests}
 * 
 * @author James Wong
 * @version 2020-01-05
 * @since v1.0.0
 */
public class DingtalkMessageNotifierTests {

    static String test_appKey = "dingbhyrzjxx6qjhjcdr";
    static String test_appSecret = "9N8nPoRB-gyeYXUJMHDU3YauNIwHVk5wtUEVROp5XgDsPjlu47bKl_xN067lQPzK";
    static String test_accessToken = "03ac6a7029ab339387b301e29e7ec094";
    static String test_mobile = "18007448807";
    static String test_templateId = "4ba6847f-b9b0-42ca-96ea-22c4ed8a3fbd";
    static String test_userId = "6165471647114842627";
    static String test_openConversationId = "cida2/N03cn0u2YXFI1iNujJQ==";
    static String test_robotCode = "dingbhyrzjxx6qjhjcdr";

    @Test
    public void testGetAccessToken() {
        final AccessTokenResult result = DingtalkMessageNotifier
                .getAccessToken(AccessToken.builder().appKey(test_appKey).appSecret(test_appSecret).build());
        System.out.println(result);
    }

    @Test
    public void testGetUserIdByMobile() {
        final GetUserIdByMobileResult result = DingtalkMessageNotifier.getUserIdByMobile(test_accessToken, test_mobile);
        System.out.println(result);
    }

    @Test
    public void testCreateSceneGroupV2() {
        final CreateSceneGroupV2Result result = DingtalkMessageNotifier.createSceneGroupV2(test_accessToken,
                CreateSceneGroupV2.builder()
                        .title("测试群-01")
                        .template_id(test_templateId)
                        .owner_user_id(test_userId)
                        .user_ids(test_userId)
                        .subadmin_ids(test_userId)
                        .build());
        System.out.println(result);
    }

    @Test
    public void testSendGroupMessageWithRobot() {
        final SampleActionCard6Param param = SampleActionCard6Param.builder()
                .title("（故障演练）异常告警")
                .text("- 告警时间: 2023-01-01 01:01:01\n- 持续时间: 10m\n- 应用服务: mqttcollect\n- 集群环境: production\n- 节点 IP: 10.0.0.112\n- 节点 CPU(10s): 200%\n- 节点 Free Mem(5m): 10%\n- 节点 InNet(1m): 1234mbps\n- 节点 OutNet(1m): 1234mbps\n- 节点 IOPS(1m): 512/1501\n- 节点 Free Disks: 99GB/250GB\n- 诊断信息: <font color='#ff0000' size=3>send_kafka_fail_rate > 30%</font>\n- **[更多指标](http://grafana.example.com/123)**")
                .buttonTitle1("Restart Now")
                .buttonUrl1("https://qq.com")
                .buttonTitle1("Cancel")
                .buttonUrl1("https://qq.com")
                .build();

        final RobotGroupMessagesSendResult result = DingtalkMessageNotifier.sendRobotGroupMessages(test_accessToken,
                RobotGroupMessagesSend.builder()
                        .msgKey(MsgKeyType.sampleActionCard6)
                        .msgParam(toJSONString(param))
                        .openConversationId(test_openConversationId)
                        .robotCode(test_robotCode)
                        .build());

        System.out.println(result);
    }

}