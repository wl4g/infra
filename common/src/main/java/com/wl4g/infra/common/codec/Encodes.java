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
package com.wl4g.infra.common.codec;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Objects.isNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;

import com.wl4g.infra.common.lang.Exceptions;

/**
 * Encapsulation of various formats of encoding and decoding tools class. </br>
 * 1. Commons-Codec hex/base64 encoding</br>
 * 2. Self-made base62 encoding </br>
 * 3. Commons-Lang xml/html escape</br>
 * 4. URLEncoder provided by JDK
 * 
 * @author calvin
 * @version 2013-01-15
 */
public abstract class Encodes {
    private static final String DEFAULT_URL_ENCODING = "UTF-8";
    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Gets string bytes.
     */
    public static byte[] toBytes(@Nullable String input) {
        if (isNull(input)) {
            return null;
        }
        if (Objects.isNull(input)) {
            return null;
        }
        return input.getBytes(UTF_8);
    }

    /**
     * Hex编码.
     */
    public static String encodeHex(@Nullable byte[] input) {
        if (isNull(input)) {
            return null;
        }
        return new String(Hex.encodeHex(input));
    }

    /**
     * Hex解码.
     */
    public static byte[] decodeHex(@Nullable String input) {
        if (isNull(input)) {
            return null;
        }
        try {
            return Hex.decodeHex(input.toCharArray());
        } catch (DecoderException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(@Nullable byte[] input) {
        if (isNull(input)) {
            return null;
        }
        return new String(Base64.encodeBase64(input));
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(@Nullable String input) {
        if (isNull(input)) {
            return null;
        }
        try {
            return new String(Base64.encodeBase64(input.getBytes(DEFAULT_URL_ENCODING)));
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * Base64编码, URL安全(将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548).
     */
    public static byte[] encodeUrlSafeBase64(@Nullable byte[] input) {
        if (isNull(input)) {
            return null;
        }
        return Base64.encodeBase64URLSafe(input);
    }

    /**
     * Base64解码.
     */
    public static byte[] decodeBase64(@Nullable String input) {
        if (isNull(input)) {
            return null;
        }
        return Base64.decodeBase64(input.getBytes());
    }

    /**
     * Base64解码.
     */
    public static String decodeBase64String(@Nullable String input) {
        if (isNull(input)) {
            return null;
        }
        try {
            return new String(Base64.decodeBase64(input.getBytes()), DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * Base62编码。
     */
    public static String encodeBase62(@Nullable byte[] input) {
        if (isNull(input)) {
            return null;
        }
        char[] chars = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
        }
        return new String(chars);
    }

    /**
     * Html 转码.
     */
    public static String escapeHtml(@Nullable String html) {
        if (isNull(html)) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(html);
    }

    /**
     * Html 解码.
     */
    public static String unescapeHtml(@Nullable String htmlEscaped) {
        if (isNull(htmlEscaped)) {
            return null;
        }
        return StringEscapeUtils.unescapeHtml4(htmlEscaped);
    }

    /**
     * Xml 转码.
     */
    public static String escapeXml(@Nullable String xml) {
        if (isNull(xml)) {
            return null;
        }
        return StringEscapeUtils.escapeXml10(xml);
    }

    /**
     * Xml 解码.
     */
    public static String unescapeXml(@Nullable String xmlEscaped) {
        if (isNull(xmlEscaped)) {
            return null;
        }
        return StringEscapeUtils.unescapeXml(xmlEscaped);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(@Nullable String part) {
        if (isNull(part)) {
            return null;
        }
        try {
            return URLEncoder.encode(part, DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(@Nullable String part) {
        if (isNull(part)) {
            return null;
        }
        try {
            return URLDecoder.decode(part, DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.unchecked(e);
        }
    }
}