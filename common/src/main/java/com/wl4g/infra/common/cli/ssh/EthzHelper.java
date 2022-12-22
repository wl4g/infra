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

import static ch.ethz.ssh2.ChannelCondition.CLOSED;
import static com.wl4g.infra.common.io.ByteStreamUtils.readFullyToString;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.wl4g.infra.common.function.CallbackFunction;
import com.wl4g.infra.common.function.ProcessFunction;
import com.wl4g.infra.common.lang.EnvironmentUtil;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.ServerHostKeyVerifier;
import ch.ethz.ssh2.Session;
import lombok.CustomLog;

/**
 * Ethz based SSH2 tools. </br>
 *
 * Notice: Cannot negotiate, proposals do not match.
 *
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2019年5月24日
 */
@Deprecated
@CustomLog
public class EthzHelper extends SshHelperBase<Session, SCPClient> {

    private static final EthzHelper DEFAULT = new EthzHelper();

    public static EthzHelper getInstance() {
        return DEFAULT;
    }

    // --- Transfer files. ---

    @Override
    public void scpGetFile(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            File localFile,
            String remoteFilePath) throws Exception {
        notNull(localFile, "Transfer localFile must not be null.");
        hasText(remoteFilePath, "Transfer remoteDir can't empty.");
        log.debug("SSH2 transfer file from {} to {}@{}:{}", localFile.getAbsolutePath(), user, host, remoteFilePath);

        try {
            // Transfer get file.
            doScpTransfer(host, port, user, pemPrivateKey, password, scp -> {
                try (SCPInputStream sis = scp.get(remoteFilePath); FileOutputStream fos = new FileOutputStream(localFile);) {
                    int i = 0;
                    byte[] buf = new byte[DEFAULT_TRANSFER_BUFFER];
                    while ((i = sis.read(buf)) != -1) {
                        fos.write(buf, 0, i);
                    }
                    fos.flush();
                }
            });

            log.debug("SCP get transfered: '{}' from '{}@{}:{}'", localFile.getAbsolutePath(), user, host, remoteFilePath);
        } catch (IOException e) {
            throw e;
        }

    }

    @Override
    public void scpPutFile(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            File localFile,
            String remoteDir) throws Exception {
        notNull(localFile, "Transfer localFile must not be null.");
        hasText(remoteDir, "Transfer remoteDir can't empty.");
        log.debug("SSH2 transfer file from {} to {}@{}:{}", localFile.getAbsolutePath(), user, host, remoteDir);

        try {
            // Transfer send file.
            doScpTransfer(host, port, user, pemPrivateKey, password, scp -> {
                try (SCPOutputStream sos = scp.put(localFile.getName(), localFile.length(), remoteDir, "0744");
                        FileInputStream fis = new FileInputStream(localFile);) {
                    int i = 0;
                    byte[] buf = new byte[DEFAULT_TRANSFER_BUFFER];
                    while ((i = fis.read(buf)) != -1) {
                        sos.write(buf, 0, i);
                    }
                    sos.flush();
                }
            });

            log.debug("SCP put transfered: '{}' to '{}@{}:{}'", localFile.getAbsolutePath(), user, host, remoteDir);
        } catch (IOException e) {
            throw e;
        }

    }

    @Override
    protected void doScpTransfer(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            CallbackFunction<SCPClient> processor) throws Exception {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        notNullOf(processor, "processor");

        // Fallback uses the local current user private key by default.
        if (isNull(pemPrivateKey)) {
            pemPrivateKey = getDefaultLocalUserPrivateKey();
        }
        notNull(pemPrivateKey, "Transfer pemPrivateKey can't null.");

        Connection conn = null;
        try {
            // Transfer file(put/get).
            processor.process(new SCPClient(conn = createConnection(host, port, user, pemPrivateKey, password)));
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (nonNull(conn))
                    conn.close();
            } catch (Exception e) {
                log.error("", e);
            }
        }

    }

    // --- Execution commands. ---

    @Override
    public SSHExecResult execWaitForResponse(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            long timeoutMs) throws Exception {
        return execWaitForComplete(host, port, user, pemPrivateKey, password, command, session -> {
            String message = null, errmsg = null;
            if (nonNull(session.getStdout())) {
                message = readFullyToString(session.getStdout());
            }
            if (nonNull(session.getStderr())) {
                errmsg = readFullyToString(session.getStderr());
            }
            return new SSHExecResult(session.getExitSignal(), session.getExitStatus(), message, errmsg);
        }, timeoutMs);
    }

    @Override
    public <T> T execWaitForComplete(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            ProcessFunction<Session, T> processor,
            long timeoutMs) throws Exception {
        return doExecCommand(host, port, user, pemPrivateKey, password, command, session -> {
            // Wait for completed by condition.
            session.waitForCondition((CLOSED), timeoutMs);
            return processor.process(session);
        });
    }

    @Override
    public final <T> T doExecCommand(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            ProcessFunction<Session, T> processor) throws Exception {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        notNullOf(processor, "processor");

        // Fallback uses the local current user private key by default.
        if (isNull(pemPrivateKey) && isNull(password)) {
            pemPrivateKey = getDefaultLocalUserPrivateKey();
        }

        Connection conn = null;
        Session session = null;
        try {
            // Session & send command.
            session = (conn = createConnection(host, port, user, pemPrivateKey, password)).openSession();
            log.info("SSH2 sending command to {}@{}, ({})", user, host, command);

            session.execCommand(command, "UTF-8");
            return processor.process(session);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (nonNull(session))
                    session.close();
            } catch (Exception e2) {
                log.error("Closing ethz session.", e2);
            }
            try {
                if (nonNull(conn))
                    conn.close();
            } catch (Exception e2) {
                log.error("Closing ethz connection.", e2);
            }
        }
    }

    /**
     * create ssh2 connection.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @return
     * @throws IOException
     */
    Connection createConnection(String host, int port, String user, char[] pemPrivateKey, String password) throws IOException {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        // notNullOf(pemPrivateKey, "pemPrivateKey");
        isTrueOf(nonNull(pemPrivateKey) || isNotBlank(password), "pemPrivateKey");

        final Connection conn = new Connection(host);
        conn.connect(ALL_TRUST, CONNECTION_TIMEOUT, KEX_TIMEOUT);

        if (nonNull(pemPrivateKey)) {
            isTrue(conn.authenticateWithPublicKey(user, pemPrivateKey, null),
                    format("Failed to SSH authenticate with %s@%s privateKey(%s)", user, host, new String(pemPrivateKey)));
        } else {
            isTrue(conn.authenticateWithPassword(user, password),
                    format("Failed to SSH authenticate with %s@%s password(%s)", user, host, password));
        }

        log.debug("SSH2 connected to {}@{}", user, host);
        return conn;
    }

    // --- Tool function's. ---

    @Override
    public SSHKeyPair generateKeypair(AlgorithmType type, String comment) throws Exception {
        throw new UnsupportedOperationException();
    }

    private static final ServerHostKeyVerifier ALL_TRUST = new ServerHostKeyVerifier() {
        @Override
        public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey)
                throws Exception {
            return true;
        }
    };

    private static final int CONNECTION_TIMEOUT = EnvironmentUtil.getIntProperty("ETHZ_SSH_CONNECTION_TIMEOUT", 3_000);
    private static final int KEX_TIMEOUT = EnvironmentUtil.getIntProperty("ETHZ_SSH_KEX_TIMEOUT", 20_000);

}