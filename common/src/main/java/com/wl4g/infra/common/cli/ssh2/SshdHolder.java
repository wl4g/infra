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
package com.wl4g.infra.common.cli.ssh2;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Iterator;
import java.util.Objects;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.scp.client.DefaultScpClientCreator;
import org.apache.sshd.scp.client.ScpClient;

import com.google.common.annotations.Beta;
import com.wl4g.infra.common.function.CallbackFunction;
import com.wl4g.infra.common.function.ProcessFunction;

import lombok.CustomLog;

/**
 * Sshd based SSH2 tools.
 *
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2019年5月24日
 * @since
 */
@Beta
@CustomLog
public class SshdHolder extends SSH2Holders<ChannelExec, ScpClient> {

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
        notNullOf(localFile, "localFile");
        hasTextOf(remoteFilePath, "remoteFilePath");

        log.debug("SSH2 transfer file from {} to {}@{}:{}", localFile.getAbsolutePath(), user, host, remoteFilePath);
        try {
            // Transfer get file.
            doScpTransfer(host, port, user, pemPrivateKey, password, scp -> {
                scp.download(remoteFilePath, localFile.getAbsolutePath());
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
        notNullOf(localFile, "localFile");
        hasTextOf(remoteDir, "remoteDir");

        log.debug("SSH2 transfer file from {} to {}@{}:{}", localFile.getAbsolutePath(), user, host, remoteDir);
        try {
            // Transfer send file.
            doScpTransfer(host, port, user, pemPrivateKey, password, scp -> {
                scp.upload(localFile.getAbsolutePath(), remoteDir);
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
            CallbackFunction<ScpClient> processor) throws Exception {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        notNullOf(processor, "processor");

        // Fallback uses the local current user private key by default.
        if (isNull(pemPrivateKey)) {
            pemPrivateKey = getDefaultLocalUserPrivateKey();
        }
        notNullOf(pemPrivateKey, "pemPrivateKey");

        SshClient client = null;
        ClientSession session = null;
        ScpClient scpClient = null;
        try {
            client = SshClient.setUpDefaultClient();
            client.start();
            session = authWithPrivateKey(client, host, port, user, pemPrivateKey, password);
            scpClient = DefaultScpClientCreator.INSTANCE.createScpClient(session);
            processor.process(scpClient);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (nonNull(session)) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("Closing session failure", e);
            }
            try {
                if (nonNull(client)) {
                    client.stop();
                    client.close();
                }
            } catch (Exception e) {
                log.error("Closing client failure", e);
            }
        }
    }

    // --- Execution commands. ---

    public Ssh2ExecResult execWaitForResponse(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            long timeoutMs) throws Exception {
        return execWaitForComplete(host, port, user, pemPrivateKey, password, command, session -> {
            String msg = null, errmsg = null;
            if (nonNull(session.getOut())) {
                // msg = readFullyToString(session.getOut());
                msg = session.getOut().toString();
            }
            if (nonNull(session.getErr())) {
                errmsg = session.getErr().toString();
            }
            return new Ssh2ExecResult(session.getExitSignal(), session.getExitStatus(), msg, errmsg);
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
            ProcessFunction<ChannelExec, T> processor,
            long timeoutMs) throws Exception {
        return doExecCommand(host, port, user, pemPrivateKey, password, command, channelExec -> {
            // Wait for completed by condition.
            channelExec.waitFor(singleton(ClientChannelEvent.CLOSED), timeoutMs);
            return processor.process(channelExec);
        });
    }

    @Override
    public <T> T doExecCommand(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            ProcessFunction<ChannelExec, T> processor) throws Exception {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        notNullOf(processor, "processor");

        // Fallback uses the local current user private key by default.
        if (isNull(pemPrivateKey)) {
            pemPrivateKey = getDefaultLocalUserPrivateKey();
        }

        ClientSession session = null;
        ChannelExec chSession = null;
        SshClient client = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            client = SshClient.setUpDefaultClient();
            client.start();
            session = authWithPrivateKey(client, host, null, user, pemPrivateKey, password);
            // channelExec = session.createExecChannel(DEFAULT_LINUX_ENV_CMD +
            // command);
            chSession = session.createExecChannel(command);
            chSession.setErr(err);
            chSession.setOut(out);
            chSession.open();
            return processor.process(chSession);
        } catch (Exception e) {
            throw e;
        } finally {
            if (nonNull(out)) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("Closing sshd output stream failure", e);
                }
            }
            try {
                if (nonNull(chSession)) {
                    chSession.close();
                }
            } catch (Exception e) {
                log.error("Closing sshd channel failure", e);
            }
            try {
                if (nonNull(session)) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("Closing sshd session failure", e);
            }
            try {
                if (nonNull(client)) {
                    client.stop();
                    client.close();
                }
            } catch (Exception e) {
                log.error("Closing sshd client failure", e);
            }
        }
    }

    private InputStream getStrToStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            return new ByteArrayInputStream(sInputString.getBytes());
        }
        return null;
    }

    private ClientSession authWithPrivateKey(
            SshClient client,
            String host,
            Integer port,
            String user,
            char[] pemPrivateKey,
            String password) throws IOException, GeneralSecurityException {
        ClientSession session = client.connect(user, host, Objects.isNull(port) ? 22 : port).verify(10000).getSession();
        if (nonNull(pemPrivateKey)) {
            Iterable<KeyPair> keyPairs = SecurityUtils.loadKeyPairIdentities(session, null,
                    getStrToStream(new String(pemPrivateKey)), null);
            Iterator<KeyPair> iterator = keyPairs.iterator();
            if (iterator.hasNext()) {
                KeyPair next = iterator.next();
                session.addPublicKeyIdentity(next);// for password-less
            }
        } else {
            notNullOf(password, "password");
            session.addPasswordIdentity(password); // for password-based
        }
        // authentication
        AuthFuture verify = session.auth().verify(10000);
        if (!verify.isSuccess()) {
            throw new GeneralSecurityException("auth fail");
        }

        return session;
    }

    /**
     * auth with password (unused now)
     *
     * @param host
     * @param port
     * @param user
     * @param password
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    // private ClientSession authWithPassword(SshClient client, String host,
    // Integer port, String user, String password)
    // throws IOException, GeneralSecurityException {
    // ClientSession session = client.connect(user, host, Objects.isNull(port) ?
    // 22 : port).verify(10000).getSession();
    // session.addPasswordIdentity(password); // for password-based
    // // authentication
    // AuthFuture verify = session.auth().verify(10000);
    // if (!verify.isSuccess()) {
    // throw new GeneralSecurityException("auth fail");
    // }
    // return session;
    // }

    // --- Tool function's. ---
    @Override
    public Ssh2KeyPair generateKeypair(AlgorithmType type, String comment) throws Exception {
        throw new UnsupportedOperationException();
    }

}