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

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.wl4g.infra.common.io.ByteStreamUtils;
import com.wl4g.infra.common.lang.EnvironmentUtil;
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
import com.wl4g.infra.common.web.server.SimpleHTTPServer;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.ContextHandler;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.Request;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.Response;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.VirtualHost;

/**
 * {@link DingtalkMessageNotifierTests}
 * 
 * @author James Wong
 * @version 2020-01-05
 * @since v1.0.0
 */
public class DingtalkMessageNotifierTests {

    static String test_appKey = EnvironmentUtil.getStringProperty("TEST_APP_KEY", "dingbhyrzjxx6qjhjcdr");
    static String test_appSecret = EnvironmentUtil.getStringProperty("TEST_APP_SECRET", "");
    static String test_accessToken = EnvironmentUtil.getStringProperty("TEST_ACCESS_TOKEN", "03ac6a7029ab339387b301e29e7ec094");
    static String test_mobile = EnvironmentUtil.getStringProperty("TEST_MOBILE", "180xxxxxxxx");
    static String test_templateId = EnvironmentUtil.getStringProperty("TEST_TEMPLATE_ID", "4ba6847f-b9b0-42ca-96ea-22c4ed8a3fbd");
    static String test_userId = EnvironmentUtil.getStringProperty("TEST_USER_ID", "6165471647114842627");

    static String test_openConversationId = EnvironmentUtil.getStringProperty("TEST_OPEN_CONVERSATION_ID",
            "cida2/N03cn0u2YXFI1iNujJQ==");

    static String test_robotCode = EnvironmentUtil.getStringProperty("TEST_ROBOT_CODE", "dingbhyrzjxx6qjhjcdr");

    static String test_aesToken = EnvironmentUtil.getStringProperty("TEST_AES_TOKEN", "H792gCFQzB2BP");
    static String test_aesKey = EnvironmentUtil.getStringProperty("TEST_AES_KEY", "bnGUZ3cqz6GgsG7B8LxBQeLcwYFDXKGMwXczNXBwCg9");
    // https://open.dingtalk.com/document/org/push-events?spm=a2q3p.21071111.0.0.2de165eeGAgfux
    static String test_corpId = EnvironmentUtil.getStringProperty("TEST_CORP_ID", test_appKey);

    @Test
    public void testGetAccessToken() {
        final AccessTokenResult result = DingtalkMessageNotifier
                .getAccessToken(AccessToken.builder().appKey(test_appKey).appSecret(test_appSecret).build());
        System.out.println(result);
        test_accessToken = result.getAccessToken();
    }

    @Test
    public void testGetUserIdByMobile() {
        final GetUserIdByMobileResult result = DingtalkMessageNotifier.getUserIdByMobile(test_accessToken, test_mobile);
        System.out.println(result);
        test_userId = result.getUserid();
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
        test_openConversationId = result.getResult().getOpenConversationId();
    }

    @Test
    public void testSendGroupMessageWithRobot() {
        final SampleActionCard6Param param = SampleActionCard6Param.builder()
                .title("（故障演练）异常告警")
                .text("- 告警时间: 2023-01-01 01:01:01\n- 持续时间: 10m\n- 应用服务: mqttcollect\n- 集群环境: production\n- 节点 IP: 10.0.0.112\n- 节点 CPU(10s): 200%\n- 节点 Free Mem(5m): 10%\n- 节点 InNet(1m): 1234mbps\n- 节点 OutNet(1m): 1234mbps\n- 节点 IOPS(1m): 512/1501\n- 节点 Free Disks: 99GB/250GB\n- 诊断信息: <font color='#ff0000' size=3>send_kafka_fail_rate > 30%</font>\n- **[更多指标](http://grafana.example.com/123)**")
                .buttonTitle1("Restart Now")
                .buttonUrl1("https://qq.com")
                .buttonTitle2("Cancel")
                .buttonUrl2("https://qq.com")
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

    /**
     * 1. frps configuration example:
     * 
     * <pre>
     * sudo mv /etc/frp/frps.ini /etc/frp/frps.ini.bak
     * sudo cat <<-EOF > /etc/frp/frps.ini
     * [common]
     * bind_port = 7000
     * vhost_http_port = 80
     * #vhost_https_port = 443
     * subdomain_host = your-frps.com
     * EOF
     * </pre>
     * 
     * 2. frpc configuration example:
     * 
     * <pre>
     * sudo mv /etc/frp/frpc.ini /etc/frp/frpc.ini.bak
     * sudo cat <<-EOF > /etc/frp/frpc.ini
     * [common]
     * server_addr = 8.134.xx.xxx
     * server_port = 7000
     * 
     * [local-webserver]
     * type = http
     * local_ip = 127.0.0.1
     * local_port = 8000
     * subdomain = frp-jameswong
     * #custom_domains = frp-jameswong.your-frps.com
     * EOF
     * </pre>
     * 
     * 3. Configure callback URL on Dingtalk
     * open-platform(https://open-dev.dingtalk.com/fe/app):
     * 
     * <pre>
     * https://frp-jameswong.your-frps.com
     * </pre>
     * 
     * 4. mock request example:
     * 
     * <pre>
     * curl -v -XPOST \
     * -H 'Content-Type: application/json' \
     * -d '{"encrypt":"1ojQf0NSvw2WPvW7LijxS8UvISr8pdDP+rXpPbcLGOmIBNbWetRg7IP0vdhVgkVwSoZBJeQwY2zhROsJq/HJ+q6tp1qhl9L1+ccC9ZjKs1wV5bmA9NoAWQiZ+7MpzQVq+j74rJQljdVyBdI/dGOvsnBSCxCVW0ISWX0vn9lYTuuHSoaxwCGylH9xRhYHL9bRDskBc7bO0FseHQQasdfghjkl"}' \
     * 'http://frp-jameswong.your-frps.com/dingtalk/callback?signature=111108bb8e6dbce3c9671d6fdb69d1506xxxx&timestamp=1783610513&nonce=123456'
     * </pre>
     */
    public static void testProcessCallbackWithListenServe() throws Exception {
        final SimpleHTTPServer serve = new SimpleHTTPServer(8000);
        VirtualHost host = serve.getVirtualHost(null); // default host
        host.addContext("/dingtalk/callback", new ContextHandler() {
            public int serve(Request req, Response resp) throws IOException {
                String signature = req.getParams().get("signature");
                String timestamp = req.getParams().get("timestamp");
                String nonce = req.getParams().get("nonce");
                String bodyJson = ByteStreamUtils.readFullyToString(req.getBody(), "UTF-8");

                final Map<String, String> result = DingtalkMessageNotifier.processCallback(test_aesToken, test_aesKey,
                        test_corpId, signature, timestamp, nonce, bodyJson, eventJson -> {
                            System.out.println("eventJson: " + eventJson);
                        });

                System.out.println("Reply message: " + result);
                resp.send(200, toJSONString(result));
                return 0;
            }
        }, "GET", "POST");
        System.out.println(DingtalkMessageNotifierTests.class.getSimpleName() + " : " + " Listen serve on 8000");
        serve.start();
    }

    public static void main(String[] args) throws Exception {
        testProcessCallbackWithListenServe();
    }

}