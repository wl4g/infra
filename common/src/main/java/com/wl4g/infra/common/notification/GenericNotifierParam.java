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
package com.wl4g.infra.common.notification;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ValidateOnExecution;

import lombok.Getter;

/**
 * {@link GenericNotifierParam}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年3月14日 v1.0.0
 * @see
 */
@Getter
@ValidateOnExecution
public class GenericNotifierParam implements NotifierParam {
    private static final long serialVersionUID = 7422435702403504747L;

    /**
     * The notification target objects.
     */
    private @NotEmpty List<String> toObjects = synchronizedList(new ArrayList<>(4));

    /**
     * Notification message template key-name.
     */
    private @Nullable String templateKey;

    /**
     * The value list of the notification message content placeholder parameter.
     */
    private @NotNull Map<String, Object> parameters = new HashMap<String, Object>(4) {
        private static final long serialVersionUID = 1299361493607274200L;

        @Override
        public Object get(Object key) {
            return super.get(key);
        }
    };

    /**
     * The notification receipt ID, which can be used to process reliable
     * message confirmation application (optional), string type, 1-15 bytes
     * long.
     */
    private @Nullable String callbackId;

    public GenericNotifierParam() {
    }

    public GenericNotifierParam(@NotBlank String singleToObject) {
        addToObjects(singleToObject);
    }

    public GenericNotifierParam(@NotBlank String singleToObject, @NotBlank String templateKey) {
        addToObjects(singleToObject);
        withTemplateKey(hasTextOf(templateKey, "templateKey"));
    }

    @Override
    public List<String> getToObjects() {
        return toObjects;
    }

    /**
     * Sets notification target objects.
     * 
     * @param toObjects
     * @return
     */
    public GenericNotifierParam setToObjects(@NotEmpty List<String> toObjects) {
        this.toObjects = toObjects;
        return this;
    }

    /**
     * Add notification target objects.
     * 
     * @param toObjects
     * @return
     */
    public GenericNotifierParam addToObjects(@NotEmpty String... toObjects) {
        if (!isNull(toObjects) && toObjects.length > 0) {
            for (String s : toObjects) {
                hasText(s, "Notification to target object element must not be null.");
            }
            this.toObjects.addAll(asList(toObjects).stream().filter(t -> !isNull(t)).collect(toList()));
        }
        return this;
    }

    /**
     * Sets notification message content template ID
     * 
     * @param templateKey
     * @return
     */
    public GenericNotifierParam withTemplateKey(@Nullable String templateKey) {
        // this.templateKey = hasTextOf(templateKey, "templateKey");
        this.templateKey = templateKey;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getParameter(@NotBlank String key, Object defaultValue) {
        hasTextOf(key, "parameterKey");
        return (T) getParameters().getOrDefault(key, defaultValue);
    }

    @Override
    public String getParameterAsString(@NotBlank String key, Object defaultValue) {
        hasTextOf(key, "parameterKey");
        Object value = getParameters().getOrDefault(key, defaultValue);
        return isNull(value) ? null : value.toString();
    }

    /**
     * Add the value list of the notification message content placeholder
     * parameter.
     * 
     * @param key
     * @param value
     * @return
     */
    public GenericNotifierParam addParameter(@NotBlank String key, Object value) {
        hasTextOf(key, "parameterKey");
        // notNullOf(value, "parameterValue");
        this.parameters.put(key, value);
        return this;
    }

    /**
     * Add the value list of the notification message content placeholder
     * parameter.
     * 
     * @param parameters
     * @return
     */
    public GenericNotifierParam addParameters(Map<String, Object> parameters) {
        if (!isNull(parameters) && !parameters.isEmpty()) {
            this.parameters.putAll(parameters.entrySet()
                    .stream()
                    .filter(e -> !isNull(e.getKey()))
                    .collect(toMap(e -> e.getKey(), e -> e.getValue())));
        }
        return this;
    }

    /**
     * Sets notification receipt ID, which can be used to process reliable
     * message confirmation application (optional), string type, 1-15 bytes
     * long.
     * 
     * @param callbackId
     * @return
     */
    public GenericNotifierParam withCallbackId(@NotBlank String callbackId) {
        this.callbackId = callbackId;
        return this;
    }

}