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
package com.wl4g.infra.context.logging;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML;
import static org.springframework.http.MediaType.APPLICATION_CBOR;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;
import static org.springframework.http.MediaType.APPLICATION_RSS_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_MARKDOWN;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.common.io.ByteStreams;
import com.wl4g.infra.common.lang.TypeConverts;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.context.logging.config.LoggingMessageProperties;
import com.wl4g.infra.context.logging.reactive.BaseLoggingWebFilter;

/**
 * {@link LoggingMessageUtil}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-05-20 v3.0.0
 * @since v3.0.0
 */
public abstract class LoggingMessageUtil {

    /**
     * Determine request verbose logging level.
     * 
     * @param exchange
     * @return
     */
    public static int determineRequestVerboseLevel(LoggingMessageProperties loggingConfig, WebRequestExtractor extractor) {
        Integer requestVerboseLevel = TypeConverts
                .parseIntOrNull(extractor.getHeaderValue(loggingConfig.getVerboseLevelRequestHeader()));
        return isNull(requestVerboseLevel) ? loggingConfig.getDefaultVerboseLevel() : requestVerboseLevel;
    }

    /**
     * Check if the specified flight log level range is met.
     * 
     * @param verboseLevel
     * @param lower
     * @param upper
     * @return
     */
    public static boolean isLoglevelRange(int verboseLevel, int lower, int upper) {
        return verboseLevel >= lower && verboseLevel <= upper;
    }

    /**
     * Check if the media type of the request or response has a body.
     * 
     * @param mediaType
     * @return
     */
    public static boolean isCompatibleWithPlainBody(MediaType mediaType) {
        if (isNull(mediaType)) {
            return false;
        }
        for (MediaType media : HAS_BODY_MEDIA_TYPES) {
            if (media.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the media type is binary, i.e. file upload.
     * 
     * @param mediaType
     * @return
     */
    public static boolean isUploadStreamMedia(MediaType mediaType) {
        return nonNull(mediaType) && mediaType.isCompatibleWith(MULTIPART_FORM_DATA);
    }

    /**
     * Check if the media type is binary, i.e. file download.
     * 
     * @param mediaType
     * @return
     */
    public static boolean isDownloadStreamMedia(MediaType mediaType) {
        return nonNull(mediaType) && mediaType.isCompatibleWith(APPLICATION_OCTET_STREAM);
    }

    /**
     * Reading to logging characters from request input stream or response.
     * 
     * @param in
     * @param expectMaxLen
     * @return
     * @throws IOException
     */
    public static String readToLogString(InputStream in, int expectMaxLen) throws IOException {
        int moreMaxLen = expectMaxLen + 1;
        byte[] bs = new byte[moreMaxLen];
        ByteStreams.read(in, bs, 0, moreMaxLen);
        // When the readable data length is greater than the maximum read data
        // length, add the log suffix '...'.
        boolean flag = (bs[expectMaxLen] != 0); // Check-the-last-character-read
        String logString = new String(bs, 0, expectMaxLen, UTF_8);
        return flag ? logString.concat(" ...") : logString;
    }

    /**
     * Logging for generic HTTP headers.
     */
    public static final List<String> LOG_GENERIC_HEADERS = unmodifiableList(new ArrayList<String>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            // Standard
            add(HttpHeaders.CONTENT_TYPE);
            add(HttpHeaders.CONTENT_ENCODING);
            add(HttpHeaders.CONTENT_LENGTH);
            add(HttpHeaders.CONTENT_RANGE);
            add(HttpHeaders.CONTENT_DISPOSITION);
            add(HttpHeaders.CONNECTION);
            add(HttpHeaders.CACHE_CONTROL);
            add(HttpHeaders.COOKIE);
            add(HttpHeaders.ACCEPT);
            add(HttpHeaders.ACCEPT_ENCODING);
            add(HttpHeaders.ACCEPT_LANGUAGE);
            add(HttpHeaders.REFERER);
            add(HttpHeaders.USER_AGENT);
            add(HttpHeaders.LOCATION);
            add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
            add(HttpHeaders.SERVER);
            add(HttpHeaders.DATE);
            add(HttpHeaders.UPGRADE);
            // Extension
            add("Content-MD5");
            add("Upgrade-Insecure-Requests");
        }
    });

    /**
     * The content-type definition of the request or corresponding body needs to
     * be recorded.
     */
    public static final List<MediaType> HAS_BODY_MEDIA_TYPES = unmodifiableList(new ArrayList<MediaType>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            add(APPLICATION_JSON);
            add(TEXT_HTML);
            add(TEXT_PLAIN);
            add(TEXT_MARKDOWN);
            add(APPLICATION_FORM_URLENCODED);
            add(APPLICATION_XML);
            add(APPLICATION_ATOM_XML);
            add(APPLICATION_PROBLEM_XML);
            add(APPLICATION_CBOR);
            add(APPLICATION_RSS_XML);
        }
    });

    public static final String LOG_REQUEST_BEGIN = LINE_SEPARATOR + "--- <HTTP Request> -------" + LINE_SEPARATOR;
    public static final String LOG_REQUEST_BODY = LINE_SEPARATOR + "\\r\\n" + LINE_SEPARATOR + "{}";
    public static final String LOG_REQUEST_END = LINE_SEPARATOR + "EOF" + LINE_SEPARATOR;
    public static final String LOG_RESPONSE_BEGIN = LINE_SEPARATOR + "--- <HTTP Response> ------" + LINE_SEPARATOR;
    public static final String LOG_RESPONSE_BODY = LINE_SEPARATOR + "\\r\\n" + LINE_SEPARATOR + "{}";
    public static final String LOG_RESPONSE_END = LINE_SEPARATOR + "EOF" + LINE_SEPARATOR;
    public static final String VAR_ROUTE_ID = "routeId";
    public static final String KEY_START_TIME = BaseLoggingWebFilter.class.getName() + ".startTime";
    public static final String KEY_VERBOSE_LEVEL = BaseLoggingWebFilter.class.getName() + ".verboseLevel";
    public static final String KEY_LOG_RECORD = BaseLoggingWebFilter.class.getName() + ".logRecord";

}
