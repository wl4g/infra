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

import static com.wl4g.component.common.io.FileIOUtils.ensureDir;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.notEmptyOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.StringUtils2.getFilenameExtension;
import static java.io.File.separator;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * {@link CompressUtils}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-26
 * @sine v1.0.0
 * @see
 */
public abstract class CompressUtils {

	// --- ZIP. ---

	/**
	 * Using zip algorithm to compress the packing directory.
	 * 
	 * @param srcDir
	 * @param outZipFilename
	 */
	public static void zip(@NotBlank String srcDir, @NotBlank String outZipFilename) {
		hasTextOf(srcDir, "srcDir");
		hasTextOf(outZipFilename, "outZipFilename");
		try (OutputStream fos = new FileOutputStream(outZipFilename);) {
			zip(srcDir, fos);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Using zip algorithm to compress output stream.
	 * 
	 * @param srcDir
	 * @param outZipFilename
	 */
	public static void zip(@NotBlank String srcDir, @NotNull OutputStream outZipStream) {
		hasTextOf(srcDir, "srcDir");
		notNullOf(outZipStream, "outZipStream");

		try (OutputStream bos = new BufferedOutputStream(outZipStream);
				ArchiveOutputStream aos = new ZipArchiveOutputStream(bos)) {
			Path dirPath = Paths.get(srcDir);
			Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					ArchiveEntry entry = new ZipArchiveEntry(dir.toFile(), dirPath.relativize(dir).toString());
					aos.putArchiveEntry(entry);
					aos.closeArchiveEntry();
					return super.preVisitDirectory(dir, attrs);
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					ArchiveEntry entry = new ZipArchiveEntry(file.toFile(), dirPath.relativize(file).toString());
					aos.putArchiveEntry(entry);
					try (InputStream in = new FileInputStream(file.toFile())) {
						IOUtils.copy(in, aos);
					}
					aos.closeArchiveEntry();
					return super.visitFile(file, attrs);
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Using zip algorithm to uncompress the zipfile.
	 * 
	 * @param zipFilename
	 * @param outDir
	 */
	public static void unzip(@NotBlank String zipFilename, @NotBlank String outDir) {
		hasTextOf(zipFilename, "zipFileName");
		hasTextOf(outDir, "outDir");

		try (InputStream fis = Files.newInputStream(Paths.get(zipFilename));
				InputStream bis = new BufferedInputStream(fis);
				ArchiveInputStream ais = new ZipArchiveInputStream(bis)) {
			ArchiveEntry entry;
			while (nonNull(entry = ais.getNextEntry())) {
				if (!ais.canReadEntryData(entry)) {
					continue;
				}
				File f = new File(filename(outDir, entry.getName()));
				if (entry.isDirectory()) {
					if (!f.isDirectory() && !f.mkdirs()) {
						f.mkdirs();
					}
				} else {
					File parent = f.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}
					try (OutputStream o = Files.newOutputStream(f.toPath())) {
						IOUtils.copy(ais, o);
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	// --- TAR. ---

	/**
	 * Using tar algorithm archive to output stream.
	 * 
	 * @param files
	 * @param outTarStream
	 */
	public static void tar(@NotEmpty List<File> files, @NotNull OutputStream outTarStream) {
		notEmptyOf(files, "files");
		notNullOf(outTarStream, "outTarStream");

		try (TarArchiveOutputStream tos = new TarArchiveOutputStream(outTarStream);) {
			for (File f : files) {
				TarArchiveEntry entry = new TarArchiveEntry(getFilenameExtension(f.getName()));
				entry.setSize(f.length());
				tos.putArchiveEntry(entry);
				// Small files write directly to memory
				if (f.length() <= DEFAULT_BUFFER_BYTES_OVERFLOW) {
					tos.write(com.google.common.io.Files.toByteArray(f));
				} else { // Copy large files in chunks
					FileIOUtils.copyFile(f, outTarStream);
				}
				tos.closeArchiveEntry();
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Using tar algorithm to uncompress the tarfile.
	 * 
	 * @param in
	 * @param outTarDir
	 * @return
	 */
	public static List<String> untar(@NotNull InputStream in, @NotBlank String outTarDir) {
		notNullOf(in, "inputStream");
		hasTextOf(outTarDir, "outTarDir");

		List<String> filenames = new ArrayList<>();
		try (TarArchiveInputStream tarIn = new TarArchiveInputStream(in, DEFAULT_BUFFER_BYTES);) {
			TarArchiveEntry entry = null;
			while (!isNull(entry = tarIn.getNextTarEntry())) {
				filenames.add(entry.getName());
				if (entry.isDirectory()) {
					// Create empty directory
					ensureDir(outTarDir, entry.getName());
				} else {
					File tmpfile = new File(outTarDir.concat(separator).concat(entry.getName()));
					// Create output directory
					ensureDir(tmpfile.getParent().concat(separator), null);
					try (OutputStream out = new FileOutputStream(tmpfile);) {
						int length = 0;
						byte[] b = new byte[DEFAULT_BUFFER_BYTES];
						while ((length = tarIn.read(b)) != -1) {
							out.write(b, 0, length);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return filenames;
	}

	/**
	 * Append tar file archive entry.
	 * 
	 * @param existingTarFile
	 * @param appendFile
	 * @param appendTarEntryName
	 * @throws IOException
	 * @throws ArchiveException
	 */
	public static void appendTarArchive(File existingTarFile, File appendFile, String appendTarEntryName)
			throws IOException, ArchiveException {
		if (!existingTarFile.exists()) {
			throw new IllegalArgumentException("file is not exist");
		}

		ArchiveStreamFactory asf = new ArchiveStreamFactory();
		File tmpFile = File.createTempFile("tmp", "tar");
		try (FileInputStream fis = new FileInputStream(existingTarFile);
				ArchiveInputStream ais = asf.createArchiveInputStream(ArchiveStreamFactory.TAR, fis);

				FileOutputStream fos = new FileOutputStream(tmpFile);
				ArchiveOutputStream aos = asf.createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);) {
			// copy the existing entries
			ArchiveEntry nextEntry;
			while ((nextEntry = ais.getNextEntry()) != null) {
				if (nextEntry.getSize() <= 0) {
					continue;
				}
				aos.putArchiveEntry(nextEntry);
				IOUtils.copy(ais, aos, (int) nextEntry.getSize());
				aos.closeArchiveEntry();
			}
			// create the new entry
			TarArchiveEntry entry = new TarArchiveEntry(appendTarEntryName);
			entry.setSize(appendFile.length());
			aos.putArchiveEntry(entry);
			IOUtils.copy(new FileInputStream(appendFile), aos, (int) appendFile.length());
			aos.closeArchiveEntry();
			aos.finish();
		}

		isTrue(existingTarFile.delete(), "Unable remove existing old tar file. %s", existingTarFile);
		tmpFile.renameTo(existingTarFile);
	}

	public static TarArchiveOutputStream createTarArchiveOutputStream(String tarFilePath) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(tarFilePath, true);
		// FileIOUtils.copyFile(new File(tarFilePath), fileOutputStream);
		return new TarArchiveOutputStream(fileOutputStream);
	}

	public static void appendToStream(TarArchiveOutputStream tout, String srcPath, String pathInTar, String fileName)
			throws IOException {
		byte[] b = new byte[1024];
		int len;
		// 构建一个Entry
		TarArchiveEntry e = new TarArchiveEntry(pathInTar + fileName);
		// 设置Entry大小，这一步必须得有，否则会报错
		e.setSize(new File(srcPath + fileName).length());
		// put一个Entry
		tout.putArchiveEntry(e);
		// 写入文件
		InputStream in = new FileInputStream(srcPath + fileName);
		while ((len = in.read(b)) != -1) {
			tout.write(b, 0, len);
		}
		in.close();
		// 关闭Entry
		tout.closeArchiveEntry();
	}

	// --- GZIP. ---
	// TODO

	/**
	 * Concat get filename
	 * 
	 * @param destDir
	 * @param name
	 * @return
	 */
	private static final String filename(String destDir, String name) {
		return destDir.concat(separator).concat(name);
	}

	public static final int DEFAULT_BUFFER_BYTES = 4096;
	public static final int DEFAULT_BUFFER_BYTES_OVERFLOW = 10 * 1024 * 1024;

}
