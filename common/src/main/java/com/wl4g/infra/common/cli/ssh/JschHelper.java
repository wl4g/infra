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

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.google.common.annotations.Beta;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.wl4g.infra.common.function.CallbackFunction;
import com.wl4g.infra.common.function.ProcessFunction;

/**
 * Jsch based SSH2 tools.
 *
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2019年5月24日
 * @since
 */
@Beta
// @CustomLog
public class JschHelper extends SshHelperBase<Void, Void> {

    private static final JschHelper DEFAULT = new JschHelper();

    public static JschHelper getInstance() {
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doScpTransfer(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            CallbackFunction<Void> processor) throws Exception {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T execWaitForComplete(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            ProcessFunction<Void, T> processor,
            long timeoutMs) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T doExecCommand(
            String host,
            int port,
            String user,
            char[] pemPrivateKey,
            String password,
            String command,
            ProcessFunction<Void, T> processor) throws Exception {
        throw new UnsupportedOperationException();
    }

    // --- Tool function's. ---

    @Override
    public SSHKeyPair generateKeypair(AlgorithmType type, String comment) throws Exception {
        int algType = KeyPair.RSA;
        if (type == AlgorithmType.DSA) {
            algType = KeyPair.DSA;
        } else if (type == AlgorithmType.ECDSA) {
            algType = KeyPair.ECDSA;
        }
        KeyPair kpair = KeyPair.genKeyPair(new JSch(), algType);

        // 私钥
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// 向OutPutStream中写入
        kpair.writePrivateKey(baos);
        final String privateKey = baos.toString();
        // 公钥
        baos = new ByteArrayOutputStream();
        kpair.writePublicKey(baos, comment);
        final String publicKey = baos.toString();
        kpair.dispose();

        // To rsa.pub
        // String publicKeyString =
        // RSAEncrypt.loadPublicKeyByFile(filePath,filename + ".pub");
        // System.out.println(publicKeyString.length());
        // System.out.println(publicKeyString);

        // To rsa
        // String privateKeyString =
        // RSAEncrypt.loadPrivateKeyByFile(filePath,filename);
        // System.out.println(privateKeyString.length());
        // System.out.println(privateKeyString);

        return new SSHKeyPair(privateKey, publicKey);
    }

}