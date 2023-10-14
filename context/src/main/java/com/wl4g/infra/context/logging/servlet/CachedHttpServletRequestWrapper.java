/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.context.logging.servlet;

import static com.wl4g.infra.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.infra.context.logging.LoggingMessageUtil.isUploadStreamMedia;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.wl4g.infra.context.utils.web.WebUtils3;

/**
 * {@link CachedHttpServletRequestWrapper}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-05-21 v3.0.0
 * @since v3.0.0
 * @see {{@link ContentCachingRequestWrapper}
 */
public class CachedHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private ByteArrayOutputStream cachedContent;
    private Map<String, String[]> cachedParameters = new HashMap<>();
    private ServletInputStream cachedInputStream;
    private boolean cachingBodySupport;

    public CachedHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.cachingBodySupport = isCachingBodySupport();
        initToCachedContent();
        initToCacheRequestParameters();
    }

    /**
     * Note: where the web server closes the underlying IO input stream, Source
     * code analysis see:
     * {@link org.apache.catalina.core.StandardContext#fireRequestDestroyEvent}↓
     * {@link org.apache.catalina.connector.CoyoteAdapter#service(Request, Response)}↓
     * {@link org.apache.catalina.connector.Request#finishRequest()}↓
     * {@link org.apache.catalina.connector.Response#finishResponse()}↓
     * {@link org.apache.catalina.connector.OutputBuffer#close()}↓
     * {@link org.apache.catalina.connector.Request#inputBuffer#close()}↓
     *
     * <p>
     * Summary: There is no need to manually close the underlying TCP network
     * stream, because the web server will manage it, and the in-memory byte
     * input stream (ByteArrayInputStream) corresponding to the outer wrapper
     * does not need to be manually closed, because it is essentially a cached
     * byte array, this pseudo IO stream JVM will recycle it by GC (only open
     * local file streams or network streams need to be displayed to close it)
     * </p>
     *
     * <p>
     * 总结：对应底层TCP网络流无需显示手动关闭，因为web服务器会管理好它，对于外层包装的内存式字节输入流(ByteArrayInputStream)也无需手动关闭它，
     * 因为本质上这种伪IO流就是缓存的字节数组，就是个普通对象，JVM会GC回收它（只有显示打开的本地文件流或网络流才需要显示关闭它）
     * </p>
     *
     * @see <a href="https://yaoyinglong.github.io/Blog/中间件/Tomcat/Tomcat处理响应过程/">...</a>
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return cachedInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    protected void initToCachedContent() {
        try {
            if (!cachingBodySupport) {
                // Unable cached, ignore files upload.
                this.cachedInputStream = super.getInputStream();
            }

            /*
             * Cache the input stream in order to read it multiple times. For
             * convenience, I use apache.commons IOUtils
             */
            this.cachedContent = new ByteArrayOutputStream();
            IOUtils.copy(super.getInputStream(), cachedContent);
            this.cachedInputStream = new CachedServletInputStream(cachedContent.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (isBlank(value)) {
            String[] values = cachedParameters.get(name);
            if (!isEmptyArray(values)) {
                value = values[0];
            }
        }
        return value;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        if (isEmpty(parameterMap)) {
            parameterMap = cachedParameters;
        }
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Enumeration<String> names = super.getParameterNames();
        if (!names.hasMoreElements()) {
            names = Collections.enumeration(cachedParameters.keySet());
        }
        return names;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (isEmptyArray(values)) {
            values = cachedParameters.get(name);
        }
        return values;
    }

    protected boolean isCachingBodySupport() {
        String contentType = getContentType();
        return isFormPost() && !isUploadStreamMedia(parseMediaType(contentType));
    }

    protected boolean isFormPost() {
        String contentType = getContentType();
        return (!isBlank(contentType) && contentType.contains(FORM_CONTENT_TYPE) && HttpMethod.POST.matches(getMethod()));
    }

    protected void initToCacheRequestParameters() {
        try {
            if (isFormPost()) {
                this.cachedParameters = new HashMap<>();
                WebUtils3.toQueryParams(cachedContent.toString("UTF-8")).forEach((name, value) -> {
                    this.cachedParameters.put(WebUtils3.safeDecodeURL(name),
                            new String[] { WebUtils3.safeDecodeURL(trimToEmpty(value)) });
                });
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write request parameters to cached content", ex);
        }
    }

    /* An input stream which reads the cached request body */
    private static class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream buffer;

        public CachedServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void reset() {
            this.buffer.reset();
        }

        //
        // This memory input stream does not need to be closed!
        //
        // @Override
        // public void close() throws IOException {
        // this.buffer.close();
        // }
    }

    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

}