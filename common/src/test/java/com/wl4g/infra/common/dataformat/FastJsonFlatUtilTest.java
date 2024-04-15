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
import org.junit.Test;

import static com.wl4g.infra.common.dataformat.FastJsonFlatUtil.flatten;
import static com.wl4g.infra.common.dataformat.FastJsonFlatUtil.unFlatten;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * JSON flatten utility class tests.
 *
 * @author zichao.zhang
 * @author James Wong
 */
public class FastJsonFlatUtilTest {

    @Test
    public void testFlatten() {
        // Arrange
        int fieldCount = 0;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Test");
        fieldCount++;

        JSONObject data = new JSONObject();
        jsonObject.put("data", data);


        JSONObject meta = new JSONObject();
        meta.put("metaName", "Meta");
        fieldCount++;
        meta.put("boolValue", true);
        fieldCount++;
        meta.put("intValue", 100);
        fieldCount++;
        meta.put("longValue", 100L);
        fieldCount++;
        meta.put("strValue", "Hello");
        fieldCount++;
        meta.put("nullValue", null);
        fieldCount++;

        jsonObject.put("meta", meta);

        JSONObject metaInnerObject = new JSONObject();

        metaInnerObject.put("name", "MetaInner");
        fieldCount++;

        meta.put("inner", metaInnerObject);

        // Act
        JSONObject result = flatten(jsonObject);

        // Assert
        assertEquals(fieldCount, result.size());
        assertEquals("Test", result.get("name"));
        assertEquals("Meta", result.get("meta.metaName"));
        assertEquals(true, result.get("meta.boolValue"));
        assertEquals(100, result.get("meta.intValue"));
        assertEquals(100L, result.get("meta.longValue"));
        assertEquals("Hello", result.get("meta.strValue"));
        assertNull(result.get("meta.nullValue"));
        assertEquals("MetaInner", result.get("meta.inner.name"));
    }

    @Test
    public void testUnFlatten() {
        // Arrange
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Test");


        JSONObject meta = new JSONObject();
        meta.put("metaName", "Meta");
        meta.put("boolValue", true);
        meta.put("intValue", 100);
        meta.put("longValue", 100L);
        meta.put("strValue", "Hello");
        meta.put("nullValue", null);

        jsonObject.put("meta", meta);

        JSONObject metaInnerObject = new JSONObject();

        metaInnerObject.put("name", "MetaInner");

        meta.put("inner", metaInnerObject);

        // Act
        JSONObject result = flatten(jsonObject);
        JSONObject unFlattenResult = unFlatten(result);

        // Assert
        assertEquals(jsonObject.size(), unFlattenResult.size());
        assertEquals(jsonObject.get("name"), unFlattenResult.get("name"));
        assertEquals(meta.size(), unFlattenResult.getJSONObject("meta").size());
        assertEquals(meta.get("metaName"), unFlattenResult.getJSONObject("meta").get("metaName"));
        assertEquals(meta.get("boolValue"), unFlattenResult.getJSONObject("meta").get("boolValue"));
        assertEquals(meta.get("intValue"), unFlattenResult.getJSONObject("meta").get("intValue"));
        assertEquals(meta.get("longValue"), unFlattenResult.getJSONObject("meta").get("longValue"));
        assertEquals(meta.get("strValue"), unFlattenResult.getJSONObject("meta").get("strValue"));
        assertEquals(meta.get("nullValue"), unFlattenResult.getJSONObject("meta").get("nullValue"));
        assertEquals(metaInnerObject.size(), unFlattenResult.getJSONObject("meta").getJSONObject("inner").size());
        assertEquals(metaInnerObject.get("name"), unFlattenResult.getJSONObject("meta").getJSONObject("inner").get("name"));
    }

}
