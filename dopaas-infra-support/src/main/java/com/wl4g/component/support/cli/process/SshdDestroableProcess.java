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

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Arrays.asList;
import static org.apache.sshd.client.channel.ClientChannelEvent.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;

import com.wl4g.component.support.cli.command.DestroableCommand;

/**
 * Sshd remote destroable process implements.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-12-06
 * @since
 */
public final class SshdDestroableProcess extends DestroableProcess {

	/** {@link ChannelExec} */
	private final ChannelExec session;

	public SshdDestroableProcess(String processId, DestroableCommand command, ChannelExec session) {
		super(processId, command);
		this.session = notNullOf(session, "channelExec");
	}

	@Override
	public OutputStream getStdin() {
		return session.getInvertedIn();
	}

	@Override
	public InputStream getStdout() {
		return session.getInvertedOut();
	}

	@Override
	public InputStream getStderr() {
		return session.getInvertedErr();
	}

	@Override
	public boolean isAlive() {
		return session.isOpen();
	}

	@Override
	public void destoryForcibly() {
		session.close(true);
	}

	@Override
	public void waitFor(long timeout, TimeUnit unit) throws IOException, InterruptedException {
		session.waitFor(WAIT_CONDITIONS, unit.toMillis(timeout));
	}

	@Override
	public Integer exitValue() {
		return session.getExitStatus();
	}

	private static final List<ClientChannelEvent> WAIT_CONDITIONS = asList(CLOSED);

}