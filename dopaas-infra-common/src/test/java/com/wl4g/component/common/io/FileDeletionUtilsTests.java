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
package com.wl4g.component.common.io;

import static java.lang.System.out;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import org.junit.Test;

import com.wl4g.component.common.matching.AntPathMatcher;

/**
 * Make test files or directories: </br>
 * </br>
 * 
 * <pre>
 * export TEST_BASE=/tmp/fileDeletion && \
 * mkdir -p ${TEST_BASE}/dir1 && \
 * mkdir -p ${TEST_BASE}/dir2 && \
 * mkdir -p ${TEST_BASE}/dir2/dir21 && \
 * touch ${TEST_BASE}/file.txt && \
 * touch ${TEST_BASE}/dir1/file11.txt && \
 * touch ${TEST_BASE}/dir2/file21.txt && \
 * touch ${TEST_BASE}/dir2/dir21/file2211.txt && \
 * sudo chmod -R 755 ${TEST_BASE} && \
 * cd ${TEST_BASE}/ && clear && tree
 * </pre>
 * 
 * Show files tree graph:</br>
 * </br>
 * 
 * <pre>
 * .
 * ├── dir1
 * │   └── file11.txt
 * ├── dir2
 * │   ├── dir21
 * │   │   └── file2211.txt
 * │   └── file21.txt
 * └── file.txt
 * </pre>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-28
 * @sine v1.0.0
 * @see
 */
public class FileDeletionUtilsTests {

	@Test
	public void jdkPathMatcherCase() throws Exception {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:/tmp/fileDeletion/file*.txt");
		Path rootDir = Paths.get("/tmp/fileDeletion/");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir, "*/*.txt")) {
			for (Path file : stream) {
				if (matcher.matches(file)) {
					out.println(file.getFileName());
				}
			}
		}
	}

	@Test
	public void antPathPatternCase() {
		AntPathMatcher matcher = new AntPathMatcher("/");
		out.println(matcher.match("/tmp/fileDeletion/dir2", "/tmp/fileDeletion/dir2/file21.txt")); // false
		out.println(matcher.match("/tmp/fileDeletion/dir2/", "/tmp/fileDeletion/dir2/")); // true
		out.println(matcher.match("/tmp/fileDeletion/dir2/", "/tmp/fileDeletion/dir2")); // false
		out.println(matcher.match("/tmp/fileDeletion/dir2/", "/tmp/fileDeletion/dir2/file21.txt")); // false
		out.println(matcher.match("/tmp/fileDeletion/dir2/*", "/tmp/fileDeletion/dir2/file21.txt")); // true
		out.println(matcher.match("/tmp/fileDeletion/dir2/**", "/tmp/fileDeletion/dir2/file21.txt")); // true
		out.println(matcher.match("/tmp/fileDeletion/dir2/**", "/tmp/fileDeletion/dir2/dir21/file2211.txt")); // true
		out.println(matcher.match("/tmp/fileDeletion/dir2*", "/tmp/fileDeletion/dir2/dir21/file2211.txt")); // false
		out.println(matcher.match("/tmp/fileDeletion/dir2**", "/tmp/fileDeletion/dir2/dir21/file2211.txt")); // false
	}

	@Test
	public void deleteWithPatternCase1() {
		out.println("deleteWithPatternCase1...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2", true, false);
	}

	@Test
	public void deleteWithPatternCase2() {
		out.println("deleteWithPatternCase2...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2/", true, false);
	}

	@Test
	public void deleteWithPatternCase3() {
		out.println("deleteWithPatternCase3...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2/*", false, false);
	}

	@Test
	public void deleteWithPatternCase4() {
		out.println("deleteWithPatternCase4...");
		// FileDeletionUtils.delete("/tmp/fileDeletion/dir2/**", false, false);
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2/**", true, false);
	}

	@Test
	public void deleteWithPatternCase5() {
		out.println("deleteWithPatternCase5...");
		FileDeletionUtils.delete("/tmp/fileDeletion/**/file*.txt", true, false);
	}

}
