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

import static com.wl4g.infra.common.io.ByteStreamUtils.readFullyToString;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

/**
 * {@link SshjHelperTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @author vjay
 * @version 2020年5月23日 v1.0.0
 * @see
 */
public class SshjHelperTests {

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
        // Test execute command
        SshHelperBase.SSHExecResult res = SshjHelper.getInstance()
                .execWaitForResponse("localhost", "prometheus", null, "123456", "ls -al /tmp", 60000);
        System.out.println("success=" + res.getMessage());
        System.out.println("fail=" + res.getErrmsg());
        System.out.println("exitCode=" + res.getExitCode());
    }

    @Test
    public void testScpFile() throws Exception {
        long t1 = System.currentTimeMillis();
        // Test upload file
        String loaclFile = "/Users/vjay/Downloads/elasticsearch-7.6.0-linux-x86_64.tar";
        SshjHelper.getInstance()
                .scpPutFile("10.0.0.160", "root", PRIVATE_KEY.toCharArray(), null, new File(loaclFile),
                        "$HOME/testssh/elasticsearch-7.6.0-linux-x86_64.tar");
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
    }

    @Test
    public void testConnect() throws IOException, InterruptedException {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect("10.0.0.160");
        KeyProvider keyProvider = ssh.loadKeys(new String(PRIVATE_KEY), null, null);
        ssh.authPublickey("root", keyProvider);
        Session session = ssh.startSession();
        // TODO
        // session.allocateDefaultPTY();
        Session.Shell shell = session.startShell();
        String command = "mvn -version\n";

        OutputStream outputStream = shell.getOutputStream();
        outputStream.write(command.getBytes());
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = shell.getInputStream();
        InputStream errorStream = shell.getErrorStream();

        Thread.sleep(1000);
        shell.close();
        if (nonNull(inputStream)) {
            String message = readFullyToString(inputStream);
            System.out.println(message);
        }
        if (nonNull(errorStream)) {
            String errmsg = readFullyToString(errorStream);
            System.out.println(errmsg);
        }

        session.close();
        ssh.close();

    }

}