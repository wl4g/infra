/*
 * Copyright 2023 ~ 2030 the original author or authors. James Wong <jameswong1376@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.dataformat.orc;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.util.Progressable;
import org.apache.orc.OrcConf;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.apache.orc.impl.PhysicalFsWriter;
import org.apache.orc.impl.writer.WriterEncryptionVariant;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static com.wl4g.infra.common.collection.CollectionUtils2.isEmpty;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.apache.orc.OrcFile.CompressionStrategy.COMPRESSION;

/**
 * The {@link OrcJsonHolder} class provides conversion utilities between ORC and json.
 */
@AllArgsConstructor
public abstract class OrcJsonHolder {
    private static final String DEFAULT_DATE_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final Configuration DEFAULT_CONF = new Configuration();
    private static final Path DEFAULT_DUMMY_PATH = new Path("/dev/null");
    private static final FileStatus DEFAULT_STATUS = new FileStatus(0, false, 0, 0, 0, DEFAULT_DUMMY_PATH);

    private boolean usePhysicalFsWriter;
    private @Min(0) int batchMaxSize;
    private @Nullable String timestampFormat;
    private @Nullable Properties options;

    @SuppressWarnings("unused")
    protected OrcJsonHolder() {
        this.usePhysicalFsWriter = true;
        this.batchMaxSize = 1024 * 1024;
        this.timestampFormat = DEFAULT_DATE_FORMATTER;
        this.options = new Properties() {
            {
                setProperty(OrcConf.COMPRESSION_STRATEGY.name(), COMPRESSION.name());
            }
        };
    }

    // ----- Get ORC schema from JSON -----

    /**
     * Get the ORC schema type for the given json json.
     *
     * @param jsonNode The record json jsonNode
     * @return The ORC schema type.
     */
    public TypeDescription getSchema(@NotNull Object jsonNode) {
        notNullOf(jsonNode, "jsonNode");
        return getSchemaFromJsonObject(jsonNode);
    }

    /**
     * Get the ORC schema type for the given json json.
     *
     * @param jsonNode The record json jsonNode
     * @return The ORC schema type.
     */
    protected abstract TypeDescription getSchemaFromJsonObject(@NotNull Object jsonNode);

    /**
     * Get the ORC schema type for the given array node.
     *
     * @param arrayNodeElements The array node
     * @return The ORC schema type.
     */
    @SuppressWarnings("all")
    protected TypeDescription getListSchemaFromArrayNode(Iterable arrayNodeElements) {
        final List<TypeDescription> childTypes = new ArrayList<>();
        for (Object element : arrayNodeElements) {
            childTypes.add(getSchemaFromJsonObject(element));
        }
        return TypeDescription.createList(getMergedSchemaType(childTypes));
    }

    /**
     * Get the merged ORC schema type for the given list of types.
     *
     * @param schemas The list of types
     * @return The merged ORC schema type.
     */
    protected TypeDescription getMergedSchemaType(List<TypeDescription> schemas) {
        if (isEmpty(schemas)) {
            throw new IllegalArgumentException("Cannot determine common type for an empty list");
        }
        TypeDescription mergedType = schemas.get(0);
        for (TypeDescription type : schemas) {
            mergedType = getMergedSchemaType(mergedType, type);
        }
        return mergedType;
    }

    /**
     * Get the common ORC schema type for the given two types.
     *
     * @param first  The first type
     * @param second The second type
     * @return The common ORC schema type.
     */
    protected TypeDescription getMergedSchemaType(TypeDescription first, TypeDescription second) {
        if (first.getCategory() == second.getCategory()) {
            return first;
        }
        switch (first.getCategory()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                switch (second.getCategory()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                    case LONG:
                        return TypeDescription.createLong();
                    case FLOAT:
                    case DOUBLE:
                        return TypeDescription.createDouble();
                    default:
                        throw new IllegalArgumentException("Cannot determine common type for " + first + " and " + second);
                }
            case FLOAT:
                if (second.getCategory() == TypeDescription.Category.DOUBLE) {
                    return TypeDescription.createDouble();
                } else {
                    throw new IllegalArgumentException("Cannot determine common type for " + first + " and " + second);
                }
            case DOUBLE:
                return TypeDescription.createDouble();
            default:
                throw new IllegalArgumentException("Cannot determine common type for " + first + " and " + second);
        }
    }

    // ----- Write ORC from JSON -----

    @SuppressWarnings("all")
    public FileSystem.Statistics writeToOrc(@NotNull List<?> records,
                                            @NotNull TypeDescription schema,
                                            @Nullable Byte magic,
                                            @NotNull OutputStream orcOutput) throws IOException {
        notNullOf(records, "records");
        notNullOf(schema, "schema");

        // Each json is at least 16B, which is a good number
        final ByteArrayOutputStream jsonOutput = new ByteArrayOutputStream(records.size() * 16);
        if (nonNull(magic)) {
            jsonOutput.write(magic);
        }
        // Concatenate all json records to a byte input stream with '\n' delimiters.
        for (Object record : records) {
            jsonOutput.write(toJsonByteArray(record));
            jsonOutput.write(LINE_SEPARATOR.getBytes());
        }
        return writeToOrc(jsonOutput.toByteArray(), schema, orcOutput);
    }

    @SuppressWarnings("ConstantConditions")
    public FileSystem.Statistics writeToOrc(@NotNull byte[] jsonBytes,
                                            @NotNull TypeDescription schema,
                                            @NotNull OutputStream orcOutput) throws IOException {
        notNullOf(jsonBytes, "jsonBytes");
        notNullOf(schema, "schema");

        final OrcFile.WriterOptions writerOptions = OrcFile.writerOptions(options, DEFAULT_CONF)
                                                           .setSchema(schema)
                                                           .version(OrcFile.Version.CURRENT);
        final FileSystem.Statistics stats = new FileSystem.Statistics("stream://");
        final FSDataOutputStream outputStream = new FSDataOutputStream(orcOutput, stats);
        if (usePhysicalFsWriter) {
            final PhysicalFsWriter physicalFsWriter = new PhysicalFsWriter(outputStream,
                    writerOptions, new WriterEncryptionVariant[0]);
            writerOptions.physicalWriter(physicalFsWriter);
        } else {
            writerOptions.fileSystem(new OrcStreamFileSystem(outputStream, DEFAULT_STATUS, DEFAULT_CONF));
        }
        try (final Writer writer = OrcFile.createWriter(DEFAULT_DUMMY_PATH, writerOptions)) {
            final VectorizedRowBatch rowBatch = schema.createRowBatch(batchMaxSize);
            try (FSDataInputStream inputStream = new FSDataInputStream(new PositionedByteArrayInputStream(jsonBytes));
                 final RecordReader reader = createRecordReader(inputStream, jsonBytes.length, schema, timestampFormat)) {
                while (reader.nextBatch(rowBatch)) { // There is added data in this batch?
                    writer.addRowBatch(rowBatch);
                }
            }
        }
        return stats;
    }

    protected abstract byte[] toJsonByteArray(Object record);

    protected abstract RecordReader createRecordReader(@NotNull FSDataInputStream orcInput,
                                                       @Min(0) int length,
                                                       @NotNull TypeDescription schema,
                                                       @Nullable String timestampFormat) throws IOException;

    // ----- Read ORC to JSON -----

    /**
     * Read ORC data to json node records.
     *
     * @param orcBytes The ORC input stream
     * @return The json records
     * @throws IOException If an I/O error occurs.
     */
    public List<Object> readFromOrcWithFirstBatch(@NotNull byte[] orcBytes) throws IOException {
        final List<Iterable<Object>> result = readFromOrcAsList(orcBytes);
        if (!result.isEmpty()) {
            return IteratorUtils.toList(result.get(0).iterator());
        }
        return emptyList();
    }

    /**
     * Read ORC data to json node records.
     *
     * @param orcBytes The ORC input stream
     * @return The json records
     * @throws IOException If an I/O error occurs.
     */
    public List<Iterable<Object>> readFromOrcAsList(@NotNull byte[] orcBytes) throws IOException {
        return IteratorUtils.toList(readFromOrc(new PositionedByteArrayInputStream(orcBytes), new Configuration()));
    }

    /**
     * Read ORC input stream to json node records.
     *
     * @param orcInput The orc input stream.
     * @return The records.
     * @throws IOException If an I/O error occurs.
     */
    public Iterator<Iterable<Object>> readFromOrc(@NotNull InputStream orcInput,
                                                  @Nullable Configuration conf) throws IOException {
        notNullOf(orcInput, "orcInput");

        final OrcFile.ReaderOptions options = OrcFile.readerOptions(isNull(conf) ? DEFAULT_CONF : conf);
        final FileStatus status = new FileStatus(orcInput.available(), false, 0, 0, 0, DEFAULT_DUMMY_PATH);
        options.filesystem(new OrcStreamFileSystem(new FSDataInputStream(orcInput), status, options.getConfiguration()));

        try (final Reader reader = OrcFile.createReader(DEFAULT_DUMMY_PATH, options)) {
            return readFromOrc(reader);
        }
    }

    /**
     * Read ORC file to json records.
     *
     * @param reader The orc input stream.
     * @return The records.
     * @throws IOException If an I/O error occurs.
     */
    public Iterator<Iterable<Object>> readFromOrc(Reader reader) throws IOException {
        notNullOf(reader, "reader");

        final TypeDescription rootSchema = reader.getSchema();
        final List<TypeDescription> fieldSchemas = rootSchema.getChildren();
        final List<String> fieldNames = rootSchema.getFieldNames();
        final VectorizedRowBatch batch = rootSchema.createRowBatch();
        final RecordReader orcReader = reader.rows(reader.options().schema(rootSchema));
        return new Iterator<Iterable<Object>>() {
            @Override
            public boolean hasNext() {
                try {
                    return orcReader.nextBatch(batch);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public Iterable<Object> next() {
                final Iterable<Object> arrayNode = createArrayJsonNode();
                for (int row = 0; row < batch.size; ++row) {
                    final Object recordNode = createObjectJsonNode();
                    for (int col = 0; col < batch.numCols; col++) {
                        final ColumnVector colVector = batch.cols[col];
                        putObjectJsonNode(recordNode, fieldNames.get(col), convertToJsonNode(fieldSchemas.get(col), colVector, row));
                    }
                    addArrayJsonNode(arrayNode, recordNode);
                }
                return arrayNode;
            }
        };
    }

    protected abstract Object createObjectJsonNode();

    protected abstract Iterable<Object> createArrayJsonNode();

    protected abstract void putObjectJsonNode(Object objectNode, String key, Object value);

    protected abstract void addArrayJsonNode(Object arrayNode, Object value);

    protected abstract Object convertToJsonNode(TypeDescription fieldSchema, ColumnVector colVector, int row);

    protected static class OrcStreamFileSystem extends FileSystem {
        private final FSDataInputStream input;
        private final FSDataOutputStream output;
        private final FileStatus status;

        public OrcStreamFileSystem(FSDataInputStream input, FileStatus status, Configuration conf) {
            this.input = notNullOf(input, "input");
            this.status = status;
            setConf(conf);
            this.output = null;
        }

        public OrcStreamFileSystem(FSDataOutputStream output, FileStatus status, Configuration conf) {
            this.input = null;
            this.status = status;
            setConf(conf);
            this.output = notNullOf(output, "output");
        }

        @Override
        public FSDataOutputStream create(Path path, FsPermission fsPermission, boolean b, int i, short i1, long l, Progressable progressable) {
            return output;
        }

        @Override
        public URI getUri() {
            return URI.create("stream://" + status.getPath());
        }

        @Override
        public FSDataInputStream open(Path path, int bufferSize) throws IOException {
            if (status.getPath().equals(path)) {
                return input;
            } else {
                throw new FileNotFoundException(path.toString());
            }
        }

        @Override
        public FSDataOutputStream append(Path path, int i,
                                         Progressable progressable) {
            throw new UnsupportedOperationException("Write operations on " +
                    getClass().getName());
        }

        @Override
        public boolean rename(Path path, Path path1) {
            throw new UnsupportedOperationException("Write operations on " +
                    getClass().getName());
        }

        @Override
        public boolean delete(Path path, boolean b) {
            throw new UnsupportedOperationException("Write operations on " +
                    getClass().getName());
        }

        @Override
        public void setWorkingDirectory(Path path) {
            throw new UnsupportedOperationException("Write operations on " +
                    getClass().getName());
        }

        @Override
        public Path getWorkingDirectory() {
            return status.getPath().getParent();
        }

        @Override
        public boolean mkdirs(Path path, FsPermission fsPermission) {
            throw new UnsupportedOperationException("Write operations on " +
                    getClass().getName());
        }

        @Override
        public FileStatus[] listStatus(Path path) throws IOException {
            return new FileStatus[] {getFileStatus(path)};
        }

        @Override
        public FileStatus getFileStatus(Path path) throws IOException {
            if (status.getPath().equals(path)) {
                return status;
            } else {
                throw new FileNotFoundException(path.toString());
            }
        }
    }

    /**
     * The {@link PositionedByteArrayInputStream} class provides a byte array input stream with position support.
     * <p>
     *
     * @see org/apache/orc/util/StreamWrapperFileSystem
     */
    protected static class PositionedByteArrayInputStream extends ByteArrayInputStream implements
            PositionedReadable, Seekable {

        @SuppressWarnings("unused")
        public PositionedByteArrayInputStream(byte[] buf) {
            super(buf);
        }

        @SuppressWarnings("unused")
        public PositionedByteArrayInputStream(byte[] buf, int offset, int length) {
            super(buf, offset, length);
        }

        @Override
        public int read(long position, byte[] buffer, int offset, int length) {
            if (position >= buf.length) {
                return -1;
            }
            // Calculate the actual length of the read.
            int readLength = (int) Math.min(length, buf.length - position);
            System.arraycopy(buf, (int) position, buffer, offset, readLength);
            return readLength;
        }

        @Override
        public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
            if (position >= buf.length) {
                throw new IOException("position is out of bounds");
            }
            // Calculate the actual length of the read.
            int readLength = (int) Math.min(length, buf.length - position);
            if (readLength < length) {
                throw new IOException("Not enough data to read");
            }
            System.arraycopy(buf, (int) position, buffer, offset, readLength);
        }

        @Override
        public void readFully(long position, byte[] buffer) throws IOException {
            readFully(position, buffer, 0, buffer.length);
        }

        @Override
        public void seek(long pos) throws IOException {
            if (pos < 0 || pos >= buf.length) {
                throw new IOException("position is out of bounds");
            }
            this.pos = (int) pos;
        }

        @Override
        public long getPos() {
            return this.pos;
        }

        @Override
        public boolean seekToNewSource(long targetPos) {
            if (targetPos < 0 || targetPos >= buf.length) {
                return false;
            }
            this.pos = (int) targetPos;
            return true;
        }
    }

}
