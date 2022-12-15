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

import static com.wl4g.infra.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.infra.common.io.ByteStreamUtils.readFullyToString;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import com.google.common.annotations.Beta;
import com.wl4g.infra.common.cli.ssh2.SshjHolder.CommandSessionWrapper;
import com.wl4g.infra.common.function.CallbackFunction;
import com.wl4g.infra.common.function.ProcessFunction;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.schmizz.sshj.xfer.scp.ScpCommandLine;

/**
 * SSHJ based SSH2 tools.
 *
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2019年5月24日
 * @since
 */
@Beta
public class SshjHolder extends SSH2Holders<CommandSessionWrapper, SCPFileTransfer> {

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
                scp.download(remoteFilePath, new FileSystemFile(localFile));
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
                // scp.upload(new FileSystemFile(localFile), remoteDir);
                scp.newSCPUploadClient().copy(new FileSystemFile(localFile), remoteDir, ScpCommandLine.EscapeMode.NoEscape);
            });

            log.debug("SCP put transfered: '{}' to '{}@{}:{}'", localFile.getAbsolutePath(), user, host, remoteDir);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Perform file transfer with remote host, including scp.put/upload or
     * scp.get/download.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param processor
     * @throws IOException
     */
    @Override
    protected void doScpTransfer(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            CallbackFunction<SCPFileTransfer> processor) throws Exception {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        notNullOf(processor, "processor");

        // Fallback uses the local current user private key by default.
        if (isNull(pemPrivateKey)) {
            pemPrivateKey = getDefaultLocalUserPrivateKey();
        }
        notNull(pemPrivateKey, "Transfer pemPrivateKey can't null.");

        SSHClient ssh = null;
        try {
            ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host, port);
            KeyProvider keyProvider = ssh.loadKeys(new String(pemPrivateKey), null, null);
            ssh.authPublickey(user, keyProvider);

            SCPFileTransfer scpFileTransfer = ssh.newSCPFileTransfer();

            // Transfer file(put/get).
            processor.process(scpFileTransfer);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (nonNull(ssh)) {
                    ssh.disconnect();
                    ssh.close();
                }
            } catch (Exception e) {
                log.error("Failed to closing ssh client.", e);
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
        return execWaitForComplete(host, port, user, pemPrivateKey, password, command, s -> {
            Session.Command cmd = s.getCommand();
            String message = null, errmsg = null;
            if (nonNull(cmd.getInputStream())) {
                message = readFullyToString(cmd.getInputStream());
            }
            if (nonNull(cmd.getErrorStream())) {
                errmsg = readFullyToString(cmd.getErrorStream());
            }
            return new Ssh2ExecResult(nonNull(cmd.getExitSignal()) ? cmd.getExitSignal().toString() : null, cmd.getExitStatus(),
                    message, errmsg);
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
            @NotNull ProcessFunction<CommandSessionWrapper, T> processor,
            long timeoutMs) throws Exception {
        return doExecCommand(host, port, user, pemPrivateKey, password, command, s -> {
            // Wait for completed by condition.
            s.getCommand().join(timeoutMs, MILLISECONDS);
            return processor.process(s);
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
            ProcessFunction<CommandSessionWrapper, T> processor) throws Exception {
        hasTextOf(host, "host");
        isTrueOf(port >= 1, "port>=1");
        hasTextOf(user, "user");
        notNullOf(processor, "processor");

        // Fallback uses the local current user private key by default.
        if (isNull(pemPrivateKey)) {
            pemPrivateKey = getDefaultLocalUserPrivateKey();
        }

        SSHClient ssh = null;
        Session session = null;
        Session.Command cmd = null;
        try {
            ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host);

            if (!isEmptyArray(pemPrivateKey)) {
                KeyProvider keyProvider = ssh.loadKeys(new String(pemPrivateKey), null, null);
                ssh.authPublickey(user, keyProvider);
            } else {
                notNullOf(password, "password");
                ssh.authPassword(user, password);
            }
            session = ssh.startSession();

            // Note: temporarily load according to the priority of user
            // environment > global environment, ignoring errors caused by linux
            // OS differences (such as ubuntu without /etc/bashrc file).
            command = ". /etc/profile; . /etc/bashrc; . /etc/bash.bashrc; . ~/.profile; . ~/.bashrc; " + command;
            cmd = session.exec(command);

            return processor.process(new CommandSessionWrapper(session, cmd));
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (nonNull(session)) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("Closing sshj session failure", e);
            }
            try {
                if (nonNull(ssh)) {
                    ssh.disconnect();
                    ssh.close();
                }
            } catch (Exception e) {
                log.error("Closing sshj client failure", e);
            }
        }
    }

    // --- Tool function's. ---

    @Override
    public Ssh2KeyPair generateKeypair(AlgorithmType type, String comment) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * {@link CommandSessionWrapper}
     *
     * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
     * @version v1.0 2020-10-09
     * @since
     */
    public static class CommandSessionWrapper {
        private final Session session;
        private final Session.Command command;

        public CommandSessionWrapper(Session session, Command command) {
            super();
            this.session = session;
            this.command = command;
        }

        public Session getSession() {
            return session;
        }

        public Session.Command getCommand() {
            return command;
        }

    }

}