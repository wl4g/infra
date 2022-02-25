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
package com.wl4g.infra.common.web.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.Beta;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.wl4g.infra.common.annotation.Stable;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.remoting.standard.HttpStatus;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.state;
import static com.wl4g.infra.common.remoting.standard.HttpStatus.*;
import static com.wl4g.infra.common.lang.Exceptions.getRootCausesString;
import static com.wl4g.infra.common.serialize.JacksonUtils.convertBean;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.newCode;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Generic restful response base model wrapper.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月9日
 * @since
 */
@Stable
public class RespBase<D> implements Serializable {
    private static final long serialVersionUID = 2647155468624590650L;

    private RetCode code = RetCode.OK;
    private String status = DEFAULT_STATUS; // [Extensible]
    private String requestId = DEFAULT_REQUESTID; // [Extensible]
    private String message = EMPTY;
    @SuppressWarnings("unchecked")
    private D data = (D) DEFAULT_DATA;

    public RespBase() {
        this(null);
    }

    public RespBase(RetCode retCode) {
        this(retCode, null);
    }

    public RespBase(D data, String status) {
        this(null, data);
    }

    public RespBase(RetCode retCode, D data) {
        this(retCode, null, data);
    }

    public RespBase(RetCode retCode, String message, D data) {
        this(retCode, null, message, data);
    }

    public RespBase(RetCode retCode, String status, String message, D data) {
        setCode(retCode);
        setStatus(status);
        setMessage(message);
        setData(data);
    }

    /**
     * Gets response code value.
     * 
     * @return
     */
    public int getCode() {
        return code.getErrcode();
    }

    /**
     * Sets response code of {@link RetCode}.
     * 
     * @param retCode
     */
    public void setCode(RetCode retCode) {
        if (nonNull(retCode)) {
            this.code = (RetCode) retCode;
        }
    }

    /**
     * Sets response code of {@link RetCode}.
     * 
     * @param retCode
     * @return
     */
    @JsonIgnore
    public RespBase<D> withCode(RetCode retCode) {
        setCode(retCode);
        return this;
    }

    /**
     * Sets response code of int.
     * 
     * @param retCode
     */
    public void setCode(int retCode) {
        this.code = newCode(retCode);
    }

    /**
     * Sets response code of int.
     * 
     * @param retCode
     * @return
     */
    @JsonIgnore
    public RespBase<D> withCode(int retCode) {
        setCode(retCode);
        return this;
    }

    /**
     * Gets status
     * 
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     * 
     * @param status
     */
    public void setStatus(String status) {
        if (!isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Sets status.
     * 
     * @param status
     * @return
     */
    @JsonIgnore
    public RespBase<D> withStatus(String status) {
        setStatus(status);
        return this;
    }

    /**
     * Gets current requestId.
     * 
     * @return
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets current requestId.
     * 
     * @param requestId
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Sets current requestId.
     * 
     * @param requestId
     * @return
     */
    @JsonIgnore
    public RespBase<D> withRequestId(String requestId) {
        setRequestId(requestId);
        return this;
    }

    /**
     * Gets error message text.
     * 
     * @return
     */
    public String getMessage() {
        return isBlank(message) ? code.getErrmsg() : message;
    }

    /**
     * Sets error message text.
     */
    public void setMessage(String message) {
        this.message = ErrorPromptMessageBuilder.build(code, !isBlank(message) ? message : this.message);
    }

    /**
     * Sets error message text.
     * 
     * @return
     */
    @JsonIgnore
    public RespBase<D> withMessage(String message) {
        setMessage(message);
        return this;
    }

    /**
     * Gets response data node of {@link Object}.
     * 
     * @return
     */
    public D getData() {
        return data;
    }

    // --- Expanded's. ---.

    /**
     * Sets response bean to data.
     * 
     * @param data
     */
    public void setData(D data) {
        if (isNull(data))
            return;
        if (checkDataAvailable()) // Data already payLoad ?
            throw new IllegalStateException(format(
                    "Already data payload, In order to set it successful the data node must be the initial value or empty. - %s",
                    getData()));

        this.data = data;
    }

    /**
     * Sets response bean to data.
     * 
     * @param data
     * @return
     */
    @JsonIgnore
    public RespBase<D> withData(D data) {
        setData(data);
        return this;
    }

    /**
     * Sets error throwable, does not set response status code.
     * 
     * @param th
     */
    @JsonIgnore
    public void setThrowable(Throwable th) {
        setMessage(getRootCausesString(th));
    }

    /**
     * Sets error throwable, does not set response status code.
     * 
     * @param th
     * @return
     */
    @JsonIgnore
    public RespBase<D> withThrowable(Throwable th) {
        setThrowable(th);
        return this;
    }

    /**
     * Handle exceptions, at the same time, the restful API compatible error
     * status code is automatically set. If there is no match, the default value
     * of {@link RetCode.SYS_ERR} is used
     * 
     * @param th
     */
    @JsonIgnore
    public void handleError(Throwable th) {
        withCode(getRestfulCode(th, RetCode.SYS_ERR)).withMessage(getRootCausesString(th));
    }

    /**
     * As {@link RespBase#data} convert to {@link DataMap}.
     * 
     * @see {@link RespBase#getData()}
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    @JsonIgnore
    public synchronized DataMap<Object> asMap() {
        if (isNull(getData()))
            return null;
        if (data instanceof Map) // typeof Map ?
            return (DataMap<Object>) getData();

        setData((D) convertBean(data, DataMap.class));
        return (DataMap<Object>) getData();
    }

    /**
     * Build {@link DataMap} instance for response data body.(if
     * {@link RespBase#getData()} is null)
     * 
     * @see {@link RespBase#getData()}
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @JsonIgnore
    public synchronized DataMap<Object> forMap() {
        if (!checkDataAvailable()) { // Data unalready ?
            data = (D) new DataMap<>(); // Init
        } else {
            // Convert to DataMap.
            /**
             * ###[Note(scene): This logic is to solve the data analysis of, for
             * example:{@link org.springframework.web.client.RestTemplate}.response]
             */
            if (data instanceof Map) { // e.g: LinkedHashMap
                if (!(data instanceof DataMap)) {
                    this.data = (D) new DataMap<>((Map) data);
                }
            } else {
                throw new UnsupportedOperationException(format(
                        "Illegal type compatible operation, because RespBase.data has initialized the available data, class type is: %s, and forMap() requires RespBase.data to be uninitialized or the initialized data type is must an instance of Map",
                        data.getClass()));
            }
        }
        return (DataMap<Object>) data;
    }

    /**
     * Build child node data map.
     * 
     * @param nodeKey
     * @return
     */
    @SuppressWarnings("unchecked")
    @JsonIgnore
    public synchronized DataMap<Object> forMapNode(String nodeKey) {
        hasText(nodeKey, "RespBase build datamap nodeKey name can't be empty");
        DataMap<Object> data = forMap();
        DataMap<Object> nodeMap = (DataMap<Object>) data.get(nodeKey);
        if (isNull(nodeMap)) {
            data.put(nodeKey, (D) (nodeMap = new DataMap<>()));
        }
        return nodeMap;
    }

    /**
     * Check whether the {@link RespBase#data} is available, for example, it
     * will become available payload after {@link RespBase#setData(Object)} or
     * {@link RespBase#forMap()} has been invoked.
     * 
     * @return
     */
    private boolean checkDataAvailable() {
        return nonNull(getData()) && getData() != DEFAULT_DATA;
    }

    /**
     * As convert {@link RespBase} to JSON string.
     * 
     * @return
     */
    public String asJson() {
        return toJSONString(this);
    }

    @Override
    public String toString() {
        return "RespBase [code=" + getCode() + ", status=" + getStatus() + ", message=" + getMessage() + ", data=" + getData()
                + "]";
    }

    // --- Function's. ---

    /**
     * Gets restful exceptions and corresponding response status code.
     * 
     * @param th
     * @return
     */
    public static final RetCode getRestfulCode(Throwable th) {
        return getRestfulCode(th, null);
    }

    /**
     * Gets restful exceptions and corresponding response status code.
     * 
     * @param th
     * @param defaultCode
     *            default status code
     * @return
     * @see {@link RESTfulException}
     * @see {@link FunctionalRuleRestrictException}
     * @see {@link InvalidParametersException}
     * @see {@link ServiceUnavailableException}
     */
    public static final RetCode getRestfulCode(Throwable th, RetCode defaultCode) {
        if (nonNull(th) && (th instanceof RESTfulException)) {
            return ((RESTfulException) th).getCode();
        }
        return defaultCode;
    }

    /**
     * New create {@link RespBase} instance.
     * 
     * @return
     */
    public static final <T> RespBase<T> create() {
        return create(null);
    }

    /**
     * New create {@link RespBase} instance.
     * 
     * @param status
     * @return
     */
    public static final <T> RespBase<T> create(String status) {
        return new RespBase<T>().withStatus(status);
    }

    /**
     * Checking the response status code for success.
     * 
     * @param resp
     * @return
     */
    public static final boolean isSuccess(RespBase<?> resp) {
        return resp != null && RetCode.OK.getErrcode() == resp.getCode();
    }

    /**
     * Check whether the {@link RespBase} status code is the expected value
     * 
     * @param resp
     * @param retCode
     * @return
     */
    public static final boolean eq(RespBase<?> resp, RetCode retCode) {
        return !isNull(resp) && retCode.getErrcode() == resp.getCode();
    }

    /**
     * Response data model
     * 
     * @author Wangl.sir
     * @version v1.0 2019年8月22日
     * @since
     * @param <V>
     */
    @Beta
    public static class DataMap<V> extends LinkedHashMap<String, V> {
        private static final long serialVersionUID = 741193108777950437L;

        /**
         * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
         * with the specified initial capacity and a default load factor (0.75).
         *
         * @throws IllegalArgumentException
         *             if the initial capacity is negative
         */
        public DataMap() {
        }

        /**
         * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
         * with the specified initial capacity and a default load factor (0.75).
         *
         * @param initialCapacity
         *            the initial capacity
         * @throws IllegalArgumentException
         *             if the initial capacity is negative
         */
        public DataMap(int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
         * with the specified initial capacity and load factor.
         *
         * @param initialCapacity
         *            the initial capacity
         * @param loadFactor
         *            the load factor
         * @throws IllegalArgumentException
         *             if the initial capacity is negative or the load factor is
         *             nonpositive
         */
        public DataMap(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        /**
         * Constructs an insertion-ordered <tt>LinkedHashMap</tt> instance with
         * the same mappings as the specified map. The <tt>LinkedHashMap</tt>
         * instance is created with a default load factor (0.75) and an initial
         * capacity sufficient to hold the mappings in the specified map.
         *
         * @param m
         *            the map whose mappings are to be placed in this map
         * @throws NullPointerException
         *             if the specified map is null
         */
        public DataMap(Map<String, V> m) {
            super(m);
        }

        /**
         * Constructs an empty <tt>LinkedHashMap</tt> instance with the
         * specified initial capacity, load factor and ordering mode.
         *
         * @param initialCapacity
         *            the initial capacity
         * @param loadFactor
         *            the load factor
         * @param accessOrder
         *            the ordering mode - <tt>true</tt> for access-order,
         *            <tt>false</tt> for insertion-order
         * @throws IllegalArgumentException
         *             if the initial capacity is negative or the load factor is
         *             nonpositive
         */
        public DataMap(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
        }

        @Override
        public V put(String key, V value) {
            if (isNotBlank(key) && value != null) {
                return super.put(key, value);
            }
            return null;
        }

        @Override
        public V putIfAbsent(String key, V value) {
            if (isNotBlank(key) && value != null) {
                return super.putIfAbsent(key, value);
            }
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends V> m) {
            if (!CollectionUtils2.isEmpty(m)) {
                super.putAll(m);
            }
        }

        public DataMap<V> andPut(String key, V value) {
            put(key, value);
            return this;
        }

        public DataMap<V> andPutIfAbsent(String key, V value) {
            putIfAbsent(key, value);
            return this;
        }

        public DataMap<V> andPutAll(Map<? extends String, ? extends V> m) {
            putAll(m);
            return this;
        }

    }

    /**
     * HTTP response code definitions. </br>
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月10日
     * @since
     * @see <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC1216</a>
     * @see <a href=
     *      "https://tools.ietf.org/html/rfc2324#section-2.3.2">RFC2314</a>
     */
    public abstract static class RetCode {

        /**
         * Successful code </br>
         * {@link HttpStatus.OK}
         */
        public static final RetCode OK = new RetCode(HttpStatus.OK.value(), "Ok") {
        };

        /**
         * Parameter error </br>
         * {@link HttpStatus.BAD_REQUEST}
         */
        public static final RetCode BAD_PARAMS = new RetCode(BAD_REQUEST.value(), "Bad parameters") {
        };

        /**
         * Unauthenticated </br>
         * {@link HttpStatus.UNAUTHORIZED}
         */
        public static final RetCode UNAUTHC = new RetCode(UNAUTHORIZED.value(), "Unauthenticated") {
        };

        /**
         * Unauthorized </br>
         * {@link HttpStatus.FORBIDDEN}
         */
        public static final RetCode UNAUTHZ = new RetCode(FORBIDDEN.value(), "Unauthorized") {
        };

        /**
         * Not found </br>
         * {@link HttpStatus.NOT_FOUND}
         */
        public static final RetCode NOT_FOUND_ERR = new RetCode(NOT_FOUND.value(), "Not found") {
        };

        /**
         * Business constraints </br>
         * {@link HttpStatus.NOT_IMPLEMENTED}
         */
        public static final RetCode BIZ_ERR = new RetCode(EXPECTATION_FAILED.value(), "Business restricted") {
        };

        /**
         * Business locked constraints </br>
         * {@link HttpStatus.LOCKED}
         * 
         * @see <a href="https://httpstatusdogs.com/423-locked">423-Locked</a>
         */
        public static final RetCode LOCKD_ERR = new RetCode(LOCKED.value(), "Resources locked") {
        };

        /**
         * Precondition limited </br>
         * {@link HttpStatus.PRECONDITION_FAILED}
         */
        public static final RetCode PRECONDITITE_LIMITED = new RetCode(PRECONDITION_FAILED.value(), "Precondition limited") {
        };

        /**
         * Unsuppported </br>
         * {@link HttpStatus.NOT_IMPLEMENTED}
         */
        public static final RetCode UNSUPPORTED = new RetCode(NOT_IMPLEMENTED.value(), "Unsuppported") {
        };

        /**
         * System abnormality </br>
         * {@link HttpStatus.SERVICE_UNAVAILABLE}
         */
        public static final RetCode SYS_ERR = new RetCode(SERVICE_UNAVAILABLE.value(),
                "Service unavailable, please try again later") {
        };

        /**
         * Unavailable For Legal Reasons </br>
         * {@link HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS}
         */
        public static final RetCode LEGAL_ERR = new RetCode(UNAVAILABLE_FOR_LEGAL_REASONS.value(),
                "Not available for legal reasons") {
        };

        /**
         * Name to {@link RetCode} value definitions.
         */
        private static final Map<String, RetCode> internalNameValues;

        /**
         * Code to {@link RetCode} value definitions.
         */
        private static final Map<Integer, RetCode> internalCodeValues;

        /**
         * Errors code.
         */
        private final int errcode;

        /**
         * Errors message.
         */
        private final String errmsg;

        private RetCode(int errcode, String errmsg) {
            // hasTextOf(errmsg, "errmsg");
            this.errcode = errcode;
            this.errmsg = errmsg;
        }

        /**
         * Get error code.</br>
         * If custom status code ({@link #_$$}) is used, it takes precedence.
         * 
         * @return
         */
        public final int getErrcode() {
            return errcode;
        }

        /**
         * Get error message.</br>
         * If custom status error message ({@link #_$$}) is used, it takes
         * precedence.
         * 
         * @return
         */
        public final String getErrmsg() {
            return errmsg;
        }

        /**
         * Case insensitive handling of JACKSON mapping enumeration.
         * 
         * <a href=
         * "https://www.cnblogs.com/chyu/p/9177140.htmlhttps://www.cnblogs.com/chyu/p/9177140.html">See</a>
         * 
         * @param value
         * @return
         */
        public static final RetCode of(String nameOrCode) {
            RetCode retCode = safeOf(nameOrCode);
            if (nonNull(retCode)) {
                return retCode;
            }
            throw new IllegalArgumentException(format("'%s'", nameOrCode));
        }

        /**
         * Safe convert retCode.
         * 
         * @param nameOrCode
         * @return
         */
        public static final RetCode safeOf(Object nameOrCode) {
            if (isNull(nameOrCode)) {
                return null;
            }
            RetCode retCode = internalNameValues.get(nameOrCode);
            if (isNull(retCode)) {
                return internalCodeValues.get(nameOrCode);
            }
            return retCode;
        }

        /**
         * New create custom status code
         * 
         * @param errcode
         * @return
         */
        public static final RetCode newCode(int errcode) {
            return new RetCode(errcode, null) {
            };
        }

        /**
         * New create custom status code
         * 
         * @param errcode
         * @param errmsg
         * @return
         */
        public static final RetCode newCode(int errcode, String errmsg) {
            return new RetCode(errcode, errmsg) {
            };
        }

        /**
         * Register defaults instance definitions.
         * 
         * @return
         */
        static {
            try {
                final Map<String, RetCode> nameValueMap = new HashMap<>();
                final Map<Integer, RetCode> codeValueMap = new HashMap<>();
                for (Field f : RetCode.class.getDeclaredFields()) {
                    if (isStatic(f.getModifiers()) && isFinal(f.getModifiers()) && f.getType() == RetCode.class) {
                        AccessController.doPrivileged(new PrivilegedAction<Void>() {
                            @Override
                            public Void run() {
                                f.setAccessible(true);
                                return null;
                            }
                        });

                        Object fObj = f.get(null);
                        if (fObj instanceof RetCode) {
                            RetCode retCode = (RetCode) f.get(null);
                            state(isNull(nameValueMap.putIfAbsent(f.getName(), retCode)), "");
                            state(isNull(codeValueMap.putIfAbsent(retCode.getErrcode(), retCode)), "");
                        }
                    }
                }
                internalNameValues = unmodifiableMap(nameValueMap);
                internalCodeValues = unmodifiableMap(codeValueMap);
            } catch (Exception ex) {
                throw new IllegalStateException("", ex);
            }
        }

    }

    /**
     *
     * Global errors code message prefix builder.
     *
     * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
     * @version v1.0 2019年11月7日
     * @since
     */
    public static final class ErrorPromptMessageBuilder {

        /**
         * Errors prefix definition.
         * 
         * @see {@link com.wl4g.infra.common.web.rest.RespBase#globalErrPrefix()}
         */
        private static String errorPrompt = getProperty("spring.infra.dopaas.component.resp-base.error-prompt", "API");

        /**
         * Building error message with prefix.
         * 
         * @param retCode
         * @param errmsg
         * @return
         */
        static final String build(RetCode retCode, String errmsg) {
            // Ignore display in message when response code is OK.
            if (isBlank(errmsg) || retCode == RetCode.OK) {
                return errmsg;
            }
            return format("[%s-%s] %s", errorPrompt, retCode.getErrcode(), errmsg);
        }

        /**
         * Setup global error message prefix.
         * 
         * @param errorPrompt
         */
        public static final void setPrompt(String errorPrompt) {
            // hasText(errorPrompt, "Global error prompt can't be empty.");
            if (!isBlank(errorPrompt)) {
                ErrorPromptMessageBuilder.errorPrompt = errorPrompt.replaceAll("-", "").toUpperCase(US);
            }
        }

    }

    /**
     * Default status value.
     */
    public static final String DEFAULT_STATUS = "Normal";

    /**
     * Default requestId value.
     */
    public static final String DEFAULT_REQUESTID = null;

    /**
     * Default data value.</br>
     * <font color=red>Note: can't be {@link DEFAULT_DATA} = new Object(),
     * otherwise jackson serialization will have the following error,
     * e.g.:</font>
     * 
     * <pre>
     *JsonMappingException: No serializer found for class java.lang.Object and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: com.wl4g.infra.common.web.rest.RespBase["data"])
     * </pre>
     */
    public static final Object DEFAULT_DATA = emptyMap();

}