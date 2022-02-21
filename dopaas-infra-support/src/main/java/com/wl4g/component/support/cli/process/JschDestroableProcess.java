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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import com.wl4g.component.support.cli.command.DestroableCommand;

/**
 * Jsch ssh2 destroable process implements.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-12-06
 * @since
 */
public final class JschDestroableProcess extends DestroableProcess {

	public JschDestroableProcess(String processId, DestroableCommand command) {
		super(processId, command);
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream getStdin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getStdout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getStderr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destoryForcibly() {
		// TODO Auto-generated method stub

	}

	@Override
	public void waitFor(long timeout, TimeUnit unit) throws IOException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer exitValue() {
		// TODO Auto-generated method stub
		return null;
	}

}