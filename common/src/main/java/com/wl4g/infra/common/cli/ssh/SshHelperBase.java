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

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.function.CallbackFunction;
import com.wl4g.infra.common.function.ProcessFunction;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SshHelperBase}, generic SSH2 client wrapper tool. </br>
 * Including the implementation of ethz/ssj/ssd.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
@Slf4j
public abstract class SshHelperBase<S, F> {

    //
    // --- Transfer function. ---
    //

    /**
     * Transfer get file from remote host. (SFTP)
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param localFile
     * @param remoteFilePath
     * @throws Exception
     */
    public void scpGetFile(
            @NotBlank String host,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            File localFile,
            String remoteFilePath) throws Exception {
        scpGetFile(host, 22, user, pemPrivateKey, password, localFile, remoteFilePath);
    }

    /**
     * Transfer get file from remote host. (SFTP)
     * 
     * @param host
     * @param port
     * @param user
     * @param pemPrivateKey
     * @param localFile
     * @param remoteFilePath
     * @throws Exception
     */
    public abstract void scpGetFile(
            @NotBlank String host,
            @Min(1) int port,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            File localFile,
            String remoteFilePath) throws Exception;

    /**
     * Transfer put file to remote host directory.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param localFile
     * @param remoteDir
     * @throws Exception
     */
    public void scpPutFile(
            @NotBlank String host,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            File localFile,
            String remoteDir) throws Exception {
        scpPutFile(host, 22, user, pemPrivateKey, password, localFile, remoteDir);
    }

    /**
     * Transfer put file to remote host directory.
     * 
     * @param host
     * @param port
     * @param user
     * @param pemPrivateKey
     * @param localFile
     * @param remoteDir
     * @throws Exception
     */
    public abstract void scpPutFile(
            @NotBlank String host,
            @Min(1) int port,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            File localFile,
            String remoteDir) throws Exception;

    /**
     * Perform file transfer with remote host, including scp.put/upload or
     * scp.get/download.
     * 
     * @param host
     * @param port
     * @param user
     * @param pemPrivateKey
     * @param processor
     * @throws IOException
     */
    protected abstract void doScpTransfer(
            @NotBlank String host,
            @Min(1) int port,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            @NotNull CallbackFunction<F> processor) throws Exception;

    //
    // --- Execution function. ---
    //

    /**
     * Execution commands with SSH2.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public SSHExecResult execWaitForResponse(
            @NotBlank String host,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            @NotBlank String command,
            @Min(1) long timeoutMs) throws Exception {
        return execWaitForResponse(host, 22, user, pemPrivateKey, password, command, timeoutMs);
    }

    /**
     * Execution commands with SSH2.
     * 
     * @param host
     * @param port
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public abstract SSHExecResult execWaitForResponse(
            @NotBlank String host,
            @Min(1) int port,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            @NotBlank String command,
            @Min(1) long timeoutMs) throws Exception;

    /**
     * Execution commands wait for complete with SSH2
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param processor
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public <T> T execWaitForComplete(
            @NotBlank String host,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            String command,
            @NotNull ProcessFunction<S, T> processor,
            long timeoutMs) throws Exception {
        return execWaitForComplete(host, 22, user, pemPrivateKey, password, command, processor, timeoutMs);
    }

    /**
     * Execution commands wait for complete with SSH2
     * 
     * @param host
     * @param port
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param processor
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public abstract <T> T execWaitForComplete(
            @NotBlank String host,
            @Min(1) int port,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            String command,
            @NotNull ProcessFunction<S, T> processor,
            long timeoutMs) throws Exception;

    /**
     * Execution commands with SSH2
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param processor
     * @return
     * @throws IOException
     */
    public <T> T doExecCommand(
            @NotBlank String host,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            @NotBlank String command,
            @NotNull ProcessFunction<S, T> processor) throws Exception {
        return doExecCommand(host, 22, user, pemPrivateKey, password, command, processor);
    }

    /**
     * Execution commands with SSH2
     * 
     * @param host
     * @param port
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param processor
     * @return
     * @throws IOException
     */
    public abstract <T> T doExecCommand(
            @NotBlank String host,
            @Min(1) int port,
            @NotBlank String user,
            char[] pemPrivateKey,
            String password,
            @NotBlank String command,
            @NotNull ProcessFunction<S, T> processor) throws Exception;

    /**
     * Get local current user ssh authentication private key of default.
     * 
     * @param host
     * @param user
     * @return
     * @throws Exception
     */
    protected final char[] getDefaultLocalUserPrivateKey() throws Exception {
        // Check private key.
        File privateKeyFile = new File(USER_HOME + "/.ssh/id_rsa");
        isTrue(privateKeyFile.exists(), String.format("Not found privateKey for %s", privateKeyFile));

        log.warn("Fallback use local user pemPrivateKey of: {}", privateKeyFile);
        try (CharArrayWriter cw = new CharArrayWriter(); FileReader fr = new FileReader(privateKeyFile.getAbsolutePath())) {
            char[] buff = new char[256];
            int len = 0;
            while ((len = fr.read(buff)) != -1) {
                cw.write(buff, 0, len);
            }
            return cw.toCharArray();
        }
    }

    //
    // --- Tools function. ---
    //

    /**
     * Generate keypair of SSH2 based on RSA/DSA/ECDSA.
     * 
     * @param type
     *            Algorithm type(RSA/DSA/ECDSA).
     * @param comment
     * @return
     * @throws Exception
     */
    public abstract SSHKeyPair generateKeypair(AlgorithmType type, String comment) throws Exception;

    /**
     * Default IO buffer size.
     */
    final public static int DEFAULT_TRANSFER_BUFFER = 1024 * 6;

    /**
     * {@link SSHExecResult}
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2020年1月9日 v1.0.0
     * @see
     */
    @ToString
    public static class SSHExecResult {

        /** Remote commands exit signal. */
        final private String signal;

        /** Remote commands exit code. */
        final private Integer code;

        /** Standard stdout */
        final private String stdout;

        /** Error stdout */
        final private String stderr;

        public SSHExecResult(String signal, Integer code, String stdout, String stderr) {
            super();
            this.signal = signal;
            this.code = code;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public String getSignal() {
            return signal;
        }

        public Integer getCode() {
            return code;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

    }

    /**
     * {@link SSHKeyPair}
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2020年2月4日 v1.0.0
     * @see
     */
    public static class SSHKeyPair {

        /** Generate ssh2 privateKey. */
        final private String privateKey;

        /** Generate ssh2 publicKey. */
        final private String publicKey;

        public SSHKeyPair(String privateKey, String publicKey) {
            notNullOf(privateKey, "privateKey");
            notNullOf(publicKey, "publicKey");
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

    }

    /**
     * {@link AlgorithmType}
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2020年2月4日 v1.0.0
     * @see
     */
    public static enum AlgorithmType {
        RSA, DSA, ECDSA
    }

    /**
     * Default environments path for different Linux distributions.</br>
     * e.g:
     * <p>
     * CentOS: /etc/bashrc </br>
     * Ubuntu: /etc/bash.bashrc
     * </p>
     */
    @Deprecated
    public static final String DEFAULT_LINUX_ENV_CMD = join(new String[] {
            // e.g: CentOS|Ubuntu
            "source /etc/profile",
            // e.g: CentOS
            "source /etc/bashrc",
            // e.g: Ubuntu
            "source /etc/bash.bashrc",
            // e.g: CentOS|Ubuntu
            "source ~/.profile",
            // e.g: CentOS|Ubuntu
            "source ~/.bashrc" }, " ");

}