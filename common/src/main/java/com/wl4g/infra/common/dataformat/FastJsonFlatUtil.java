/**
 * Copyright (C) 2023 ~ 2035 the original authors WL4G (James Wong).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.dataformat;

import com.alibaba.fastjson.JSONObject;
import com.google.common.annotations.Beta;
import com.wl4g.infra.common.annotation.InterfaceStability;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * JSON flatten utility class.
 *
 * @author zichao.zhang
 * @author James Wong
 */
@Beta
@InterfaceStability.Unstable
public abstract class FastJsonFlatUtil {

    /**
     * Expand the sub-objects in json and tile them in .
     * <p>
     * Note: empty objects will be lost
     *
     * @param jsonObject json object
     * @return tiled json object, returned in TreeMap mode
     */
    public static JSONObject flatten(JSONObject jsonObject) {
        final JSONObject flattenJSON = new JSONObject();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                Map<String, Object> flattenMap = flattenJSONProp(key, (JSONObject) value);
                for (String flattenKey : flattenMap.keySet()) {
                    flattenJSON.put(flattenKey, flattenMap.get(flattenKey));
                }
            } else {
                flattenJSON.put(key, value);
            }
        }
        return flattenJSON;
    }

    /**
     * Restore tiled json to json object
     *
     * @param flattenMap flat json object
     * @return Restored json object
     */
    public static JSONObject unFlatten(Map<String, Object> flattenMap) {
        JSONObject jsonObject = new JSONObject();
        for (String key : flattenMap.keySet()) {
            JSONObject current = jsonObject;

            String[] keys = StringUtils.split(key, ".");

            for (int i = 0; i < keys.length - 1; i++) {
                String subKey = keys[i];
                if (!current.containsKey(subKey)) {
                    current.put(subKey, new JSONObject());
                }
                current = current.getJSONObject(subKey);
            }
            current.put(keys[keys.length - 1], flattenMap.get(key));
        }
        return jsonObject;
    }

    private static Map<String, Object> flattenJSONProp(String key, JSONObject value) {
        final JSONObject flattenMap = new JSONObject();
        for (String subKey : value.keySet()) {
            Object subValue = value.get(subKey);
            if (subValue instanceof JSONObject) {
                Map<String, Object> flattenSubMap = flattenJSONProp(key + "." + subKey, (JSONObject) subValue);
                for (String flattenSubKey : flattenSubMap.keySet()) {
                    flattenMap.put(flattenSubKey, flattenSubMap.get(flattenSubKey));
                }
                continue;
            }
            flattenMap.put(key + "." + subKey, subValue);
        }
        return flattenMap;
    }
}
