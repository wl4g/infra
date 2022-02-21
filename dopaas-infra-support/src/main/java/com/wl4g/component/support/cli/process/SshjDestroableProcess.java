/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.component.support.cli.process;

import static com.wl4g.component.common.lang.Assert2.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import com.wl4g.component.common.cli.ssh2.SshjHolder.CommandSessionWrapper;
import com.wl4g.component.support.cli.command.DestroableCommand;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

/**
 * SSHj remote destroable process implements.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-12-06
 * @since
 */
public final class SshjDestroableProcess extends DestroableProcess {

	/**
	 * Execution remote process of session {@link Session}
	 */
	final private CommandSessionWrapper session;

	public SshjDestroableProcess(String processId, DestroableCommand command, CommandSessionWrapper session) {
		super(processId, command);
		notNull(session, "Command remote process session can't null.");
		this.session = session;
	}

	@Override
	public OutputStream getStdin() {
		return session.getCommand().getOutputStream();
	}

	@Override
	public InputStream getStdout() {
		return session.getCommand().getInputStream();
	}

	@Override
	public InputStream getStderr() {
		return session.getCommand().getErrorStream();
	}

	@Override
	public boolean isAlive() {
		return session.getSession().isOpen();
	}

	@Override
	public void destoryForcibly() {
		try {
			session.getCommand().close();
		} catch (TransportException | ConnectionException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void waitFor(long timeout, TimeUnit unit) throws IOException, InterruptedException {
		session.getCommand().join(timeout, unit);
	}

	@Override
	public Integer exitValue() {
		return session.getCommand().getExitStatus();
	}

}