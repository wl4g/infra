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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.Assert2.state;
import static java.io.File.separator;
import static java.nio.file.Files.isSymbolicLink;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

import javax.validation.constraints.NotBlank;

/**
 * {@link FileDeletionUtils}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年4月1日
 * @since
 */
public abstract class FileDeletionUtils {

	/**
	 * Delete files according to the file path matching ant pattern.
	 * 
	 * @param globPathPattern
	 *            Note: Is the standard unix glob matching pattern.
	 * @see {@link org.apache.commons.io.FileUtils#deleteDirectory(File)}
	 * @see {@link org.apache.commons.io.FileUtils#forceDelete(File)}
	 */
	public static void delete(@NotBlank String globPathPattern) {
		delete(globPathPattern, false, false);
	}

	/**
	 * Delete files according to the file path matching ant pattern.
	 * 
	 * @param globPathPattern
	 *            Note: Is the standard unix glob matching pattern
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, delete failure throws an
	 *             exception.
	 * @see {@link org.apache.commons.io.FileUtils#deleteDirectory(File)}
	 * @see {@link org.apache.commons.io.FileUtils#forceDelete(File)}
	 */
	public static void delete(@NotBlank String globPathPattern, boolean retainDirectory) {
		delete(globPathPattern, retainDirectory, false);
	}

	/**
	 * Delete files according to the file path matching ant pattern.
	 * 
	 * @param globPathPattern
	 *            Note: Is the standard unix glob matching pattern
	 * @param retainDirectory
	 * @throws IllegalStateException
	 *             When fast failure is enabled, delete failure throws an
	 *             exception.
	 * @see {@link org.apache.commons.io.FileUtils#deleteDirectory(File)}
	 * @see {@link org.apache.commons.io.FileUtils#forceDelete(File)}
	 */
	public static void delete(@NotBlank String globPathPattern, boolean retainDirectory, boolean fastfail) {
		hasTextOf(globPathPattern, "globPathPattern");

		// Start directory path.
		String startPath = globPathPattern;
		int startIndex = globPathPattern.indexOf("*");
		if (startIndex > 0) {
			startPath = globPathPattern.substring(0, startIndex);
		}
		// Clear the suffix separator. In order to match ant path, it is
		// compatible with GNU specification.
		if (!endsWith(startPath, separator)) {
			// startPath = startPath.substring(0, startPath.length() - 1);
			startPath += separator;
		}

		File file = new File(startPath);
		if (file.exists() && !isSymbolicLink(file.toPath())) {
			try {
				doDeleteFileOrDirectories(globPathPattern, file, retainDirectory, fastfail);
			} catch (IllegalStateException | IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Recursion delete sub files only or sub directories.
	 * 
	 * @param globPathPattern
	 * @param path
	 * @param retainDirectory
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, the deletion failure will throw
	 *             an exception and interrupt the execution immediately, which
	 *             may result in some files not being deleted.
	 * @throws IOException
	 */
	private static final void doDeleteFileOrDirectories(String globPathPattern, File path, boolean retainDirectory,
			boolean fastfail) throws IllegalStateException, IOException {
		notNullOf(path, "path");

		if (path.exists()) {
			if (path.isFile()) {
				if (getGlobPathMatcher(globPathPattern).matches(path.toPath())) {
					boolean result = path.delete();
					if (fastfail) {
						state(result, "Cannot to delete sub file '%s'", path);
					}
				}
			} else {
				File[] childrens = path.listFiles();
				if (nonNull(childrens)) {
					// Recursion deletion children files.
					for (File child : childrens) {
						doDeleteFileOrDirectories(globPathPattern, child, retainDirectory, fastfail);
					}
					// Delete this directory.
					if (!retainDirectory && getGlobPathMatcher(globPathPattern).matches(path.toPath())) {
						boolean result = path.delete();
						if (fastfail) {
							state(result, "Cannot to delete sub file '%s'", path);
						}
					}
				}
			}
		}
	}

	/**
	 * {@link PathMatcher}
	 * 
	 * @param globPathPattern
	 * @return
	 * @throws IOException
	 */
	private static PathMatcher getGlobPathMatcher(String globPathPattern) throws IOException {
		PathMatcher matcher = matcherCache.get();
		if (isNull(matcher)) {
			synchronized (FileDeletionUtils.class) {
				matcherCache.set(matcher = FileSystems.getDefault().getPathMatcher("glob:".concat(globPathPattern)));
			}
		}
		return matcher;
	}

	/**
	 * GNU glob {@link PathMatcher} cache.
	 */
	private static final ThreadLocal<PathMatcher> matcherCache = new ThreadLocal<>();

}