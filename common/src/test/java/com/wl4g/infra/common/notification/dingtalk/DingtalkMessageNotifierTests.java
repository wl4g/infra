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

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import org.junit.Test;

import com.wl4g.infra.common.notification.GenericNotifyMessage;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.MsgKeyType;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.SampleActionCard5Param;

/**
 * {@link DingtalkMessageNotifierTests}
 * 
 * @author James Wong
 * @version 2020-01-05
 * @since v1.0.0
 */
public class DingtalkMessageNotifierTests extends DingtalkAPITests {

    @Test
    public void testSend() {
        // @formatter:off
        //final MsgKeyType msgKey = MsgKeyType.sampleActionCard6;
        //final SampleActionCard6Param param = SampleActionCard6Param.builder()
        //        .title("（故障演练）异常告警")
        //        .text("- 告警时间: 2023-01-01 01:01:01\n- 持续时间: 10m\n- 应用服务: mqttcollect\n- 集群环境: production\n- 节点 IP: 10.0.0.112\n- 节点 CPU(10s): 200%\n- 节点 Free Mem(5m): 10%\n- 节点 InNet(1m): 1234mbps\n- 节点 OutNet(1m): 1234mbps\n- 节点 IOPS(1m): 512/1501\n- 节点 Free Disks: 99GB/250GB\n- 诊断信息: <font color='#ff0000' size=3>send_kafka_fail_rate > 30%</font>\n- **[更多指标](http://grafana.example.com/123)**")
        //        .buttonTitle1("Restart Now")
        //        .buttonUrl1("https://qq.com")
        //        .buttonTitle2("Cancel")
        //        .buttonUrl2("https://qq.com")
        //        .build();
        // @formatter:on

        // @formatter:off
        final MsgKeyType msgKey = MsgKeyType.sampleActionCard5;
        final SampleActionCard5Param param = SampleActionCard5Param.builder()
                .title("（故障演练）异常告警")
                .text("- 告警时间: 2023-01-01 01:01:01\n- 持续时间: 10m\n- 应用服务: mqttcollect\n- 集群环境: production\n- 节点 IP: 10.0.0.112\n- 节点 CPU(10s): 200%\n- 节点 Free Mem(5m): 10%\n- 节点 InNet(1m): 1234mbps\n- 节点 OutNet(1m): 1234mbps\n- 节点 IOPS(1m): 512/1501\n- 节点 Free Disks: 99GB/250GB\n- 诊断信息: <font color='#ff0000' size=3>send_kafka_fail_rate > 30%</font>\n- **[更多指标](http://grafana.example.com/123)**")
                .actionTitle1("链接1")
                .actionURL1("https://qq.com")
                .actionTitle2("链接2")
                .actionURL2("https://qq.com")
                .actionTitle3("链接3")
                .actionURL3("https://qq.com")
                .actionTitle4("链接4")
                .actionURL4("https://qq.com")
                .actionTitle5("链接5")
                .actionURL5("https://qq.com")
                .build();
        // @formatter:on

        final DingtalkNotifyProperties config = new DingtalkNotifyProperties();
        config.setAppKey(test_appKey);
        config.setAppSecret(test_appSecret);

        final DingtalkMessageNotifier notifier = new DingtalkMessageNotifier(config, null);

        final GenericNotifyMessage msg = new GenericNotifyMessage(test_openConversationId)
                .addParameter(DingtalkMessageNotifier.KEY_ACCESS_TOKEN, test_accessToken)
                .addParameter(DingtalkMessageNotifier.KEY_MSG_KEY, msgKey.name())
                .addParameter(DingtalkMessageNotifier.KEY_MSG_PARAM, toJSONString(param))
                .addParameter(DingtalkMessageNotifier.KEY_ROBOT_CODE, test_robotCode);

        final Object result = notifier.send(msg);
        System.out.println(result);
    }

}