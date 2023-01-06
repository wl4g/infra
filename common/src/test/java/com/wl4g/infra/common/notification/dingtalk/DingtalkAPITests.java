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

import static com.wl4g.infra.common.lang.EnvironmentUtil.getStringProperty;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.wl4g.infra.common.io.ByteStreamUtils;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.AccessToken;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.AccessTokenResult;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.CreateSceneGroupV2;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.CreateSceneGroupV2Result;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.GetUserIdByMobileResult;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.MsgKeyType;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.RobotGroupMessagesSend;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.RobotGroupMessagesSendResult;
import com.wl4g.infra.common.notification.dingtalk.internal.DingtalkAPI.SampleActionCard6Param;
import com.wl4g.infra.common.web.server.SimpleHTTPServer;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.ContextHandler;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.Request;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.Response;
import com.wl4g.infra.common.web.server.SimpleHTTPServer.VirtualHost;

/**
 * {@link DingtalkAPITests}
 * 
 * @author James Wong
 * @version 2020-01-05
 * @since v1.0.0
 */
public class DingtalkAPITests {

    static String test_appKey = getStringProperty("TEST_APP_KEY", "dingbhyrzjxx6qjhjcdr");
    static String test_appSecret = getStringProperty("TEST_APP_SECRET", "");
    static String test_accessToken = getStringProperty("TEST_ACCESS_TOKEN", "a74c93b76b893a3996a243ddadbdf078");
    static String test_mobile = getStringProperty("TEST_MOBILE", "180xxxxxxxx");
    static String test_templateId = getStringProperty("TEST_TEMPLATE_ID", "4ba6847f-b9b0-42ca-96ea-22c4ed8a3fbd");
    static String test_userId = getStringProperty("TEST_USER_ID", "6165471647114842627");

    static String test_openConversationId = getStringProperty("TEST_OPEN_CONVERSATION_ID", "cidG+niQ3Ny\\/NwUc5KE7mANUQ==");
    static String test_robotCode = getStringProperty("TEST_ROBOT_CODE", "dingbhyrzjxx6qjhjcdr");

    static String test_aesToken = getStringProperty("TEST_AES_TOKEN", "H792gCFQzB2BP");
    static String test_aesKey = getStringProperty("TEST_AES_KEY", "bnGUZ3cqz6GgsG7B8LxBQeLcwYFDXKGMwXczNXBwCg9");

    // https://open.dingtalk.com/document/org/push-events?spm=a2q3p.21071111.0.0.2de165eeGAgfux
    static String test_corpId = getStringProperty("TEST_CORP_ID", test_appKey);

    @Test
    public void testGetAccessToken() {
        final AccessTokenResult result = DingtalkAPI
                .getAccessToken(AccessToken.builder().appKey(test_appKey).appSecret(test_appSecret).build());
        System.out.println(result);
        test_accessToken = result.getAccessToken();
    }

    @Test
    public void testGetUserIdByMobile() {
        final GetUserIdByMobileResult result = DingtalkAPI.getUserIdByMobile(test_accessToken, test_mobile);
        System.out.println(result);
        test_userId = result.getUserid();
    }

    @Test
    public void testCreateSceneGroupV2() {
        final CreateSceneGroupV2Result result = DingtalkAPI.createSceneGroupV2(test_accessToken,
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

        final RobotGroupMessagesSendResult result = DingtalkAPI.sendRobotGroupMessages(test_accessToken,
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

                final Map<String, String> result = DingtalkAPI.processCallback(test_aesToken, test_aesKey, test_corpId, signature,
                        timestamp, nonce, bodyJson, eventJson -> {
                            System.out.println("eventJson: " + eventJson);
                        });

                System.out.println("Reply message: " + result);
                resp.send(200, toJSONString(result));
                return 0;
            }
        }, "GET", "POST");
        System.out.println(DingtalkAPITests.class.getSimpleName() + " : " + " Listen serve on 8000");
        serve.start();
    }

    public static void main(String[] args) throws Exception {
        testProcessCallbackWithListenServe();
    }

}