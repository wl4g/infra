/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
import static java.lang.System.err;
import static java.lang.System.out;

import java.io.File;

import org.junit.Test;

import com.wl4g.infra.common.io.ByteStreamUtils;

/**
 * Notice: Cannot negotiate, proposals do not match.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @author vjay
 * @version 2020年5月23日 v1.0.0
 */
@Deprecated
public class EthzHelperTests {

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
    public void testExecCommand1() throws Exception {
        String command = "ls -al /tmp/";
        EthzHelper.getInstance().execWaitForComplete("localhost", 22, "prometheus", null, "123456", command, s -> {
            err.println(ByteStreamUtils.readFullyToString(s.getStderr()));
            out.println(ByteStreamUtils.readFullyToString(s.getStdout()));
            err.println("signal:" + s.getExitSignal() + ", state:" + s.getState() + ", status:" + s.getExitStatus());
            return null;
        }, 30_000);
    }

    @Test
    public void testScpFile() throws Exception {
        long t1 = currentTimeMillis();
        // Test upload file
        String loaclFile = "/Users/vjay/Downloads/safecloud-0203.sql";
        EthzHelper.getInstance()
                .scpPutFile("10.0.0.160", "root", PRIVATE_KEY.toCharArray(), null, new File(loaclFile), "/root/testssh/");
        long t2 = currentTimeMillis();
        out.println(t2 - t1);
    }

}