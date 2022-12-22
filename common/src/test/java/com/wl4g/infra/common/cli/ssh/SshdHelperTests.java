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
package com.wl4g.infra.common.cli.ssh;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.Collections.singleton;
import static java.util.Objects.nonNull;

import java.io.File;

import org.apache.sshd.client.channel.ClientChannelEvent;
import org.junit.Test;

import com.wl4g.infra.common.cli.ssh.SshHelperBase.SSHExecResult;

/**
 * {@link SshdHelperTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @author vjay
 * @version 2020年5月23日 v1.0.0
 * @see
 */
public class SshdHelperTests {

    static String HOME = "$HOME";

    // @formatter:off
    static String PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\r\n"
            + "MIICXgIBAAKBgQDKOkJL7dYsmPkm+r7JdmvGUTXUBwCPxYgGLO2GbEjecwLUTEzg\r\n"
            + "J8A7wMy4DLb9v7s+OPJOf+lp8vjJzGuPl624EiWRMwGghx8IAUyAlDezXNzy8ajb\r\n"
            + "4aipKAICDeQpCmKtLkfac6bMRop455naCqHqahDkhjECKym1rHHok4VKCQIDAQAB\r\n"
            + "AoGBAK4S2hB76Pk4sHdSLbpDOmBadWhOorgfQ4h1Ufx853i8LXpLN31YGkwVGONw\r\n"
            + "5m+kg+v6nvDdgDFYGbmzQf83hCs9aesR2Bh6GV0cURBUL0VazUhiZ0B80fC2vwmJ\r\n"
            + "xFLVi6DN1WD3QO/GT00iWNRn1/l0v/Ert2rflDMdMikn+aoBAkEA/MIcq8jf4ksN\r\n"
            + "p1eeZykCbSVpB2HAqsl7EPjp9evhHBkcQ4m0yOuuaN4YcNbp5fWg7a7K+JWp8k6b\r\n"
            + "QtHLeBh/yQJBAMzSPP07CcbZkC7FxaGP8YZdg4lEUgvTyY6yufIIepDjtb/fLzUW\r\n"
            + "2faZmxUU/8crzpOSBDntvgNbhjpsw9vCGEECQQDPQ6nZIC6e1SbMG6hUNae8stmu\r\n"
            + "aPVh1zgokcTgmV2N+fVYWJq7y6/IZJ8sIL/Kh6JAZX4hXDDw9o6Qu5Ka15QpAkBc\r\n"
            + "27Pq2qlEDb7gdalz5d6KHDtWMDNCSXJHz5+dq1pl9dagdn7ggsuukVVN6YdMtP+i\r\n"
            + "x8BCwxYyT3w7YLQrHYQBAkEA6GWGpAkaD69uAH6Btr8SyT5rNUfY2372nJ3IX1qD\r\n"
            + "XooRGA8ggInRnroRy/JM+B7965lcFwCX4TtpZGVPrvazrA==\r\n"
            + "-----END RSA PRIVATE KEY-----";
    // @formatter:on

    @Test
    public void testExecCatCommand() throws Exception {
        String cmd = "cat /tmp/test_vim_file.txt";
        SSHExecResult resp = SshdHelper.getInstance().execWaitForResponse("127.0.0.1", "prometheus", null, "123456", cmd, 3_000);
        out.println("stdout=" + resp.getMessage());
        out.println("stderr=" + resp.getErrmsg());
        out.println("exitCode=" + resp.getExitCode());
    }

    @Test
    public void testExecVimCommand() throws Exception {
        String cmd = "vim /tmp/test_vim_file.txt";
        SshdHelper.getInstance().doExecCommand("127.0.0.1", "jameswong", PRIVATE_KEY.toCharArray(), null, cmd, chSession -> {
            chSession.waitFor(singleton(ClientChannelEvent.CLOSED), 3_000);
            String msg = null, errmsg = null;
            if (nonNull(chSession.getOut())) {
                msg = chSession.getOut().toString();
                out.println("stdout=" + msg);
            }
            if (nonNull(chSession.getErr())) {
                errmsg = chSession.getErr().toString();
                out.println("stderr=" + errmsg);
            }
            return null;
        });
    }

    @Test
    public void testPutTransfer() throws Exception {
        long begin = currentTimeMillis();
        // Test upload file
        String loaclFile = "/Users/vjay/Downloads/elasticsearch-7.6.0-linux-x86_64.tar";
        SshdHelper.getInstance()
                .scpPutFile("10.0.0.160", "root", PRIVATE_KEY.toCharArray(), null, new File(loaclFile),
                        "$HOME/testssh/elasticsearch-7.6.0-linux-x86_64.tar");
        long end = currentTimeMillis();
        out.println("cost:" + (end - begin));// 80801ms 150<cpu<200
    }

}