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
package com.wl4g.infra.common.function;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.out;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wl4g.infra.common.function.TreeConvertor.TreeNode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link TreeConvertorTests}
 *
 * @author James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2017-09-08
 * @since
 */
public class TreeConvertorTests {

    @Test
    public void testSimpleTreeParse() {
        // 创建树结构转换器
        final TreeConvertor<String, MyNode> convertor = new TreeConvertor<>();

        // 将平面树转为children树
        final List<MyNode> treeNode = convertor.formatToChildren(TEST_FLAT_NODES, false);
        final String treeNodeJson = toJSONString(treeNode);
        out.println("----------------\n" + treeNodeJson);
        assert nonNull(treeNode) && treeNode.size() == 2;

        final List<MyNode> treeNode2 = parseJSON(treeNodeJson, new TypeReference<List<MyNode>>() {
        });
        assert nonNull(treeNode2) && treeNode2.size() == 2;

        // 获取父节点下所有子孙节点（包括本身）
        final List<MyNode> treeChildrenOf11 = convertor.subChildrens(treeNode2, "11");
        out.println("----------------\n" + toJSONString(treeChildrenOf11));
        assert nonNull(treeChildrenOf11) && treeChildrenOf11.size() == 1 && treeChildrenOf11.get(0).getChildrens().size() == 3;

        // 将children树转为平面树
        out.println("----------------\n" + toJSONString(convertor.parseChildren(treeNode)));
    }

    @Getter
    @Setter
    @ToString
    public static class MyNode implements TreeNode<String, MyNode> {
        private static final long serialVersionUID = 3429949759108637800L;
        // --- Tree node basic. ---

        private String id;
        private String name;
        private String parentId;
        private int level;
        private List<MyNode> childrens;

        // --- Node statistics. ---

        private int count;
        private Double sum;
        private Double value;

        public MyNode() {
            super();
        }

        public MyNode(String id, String parentId, String name) {
            super();
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        public MyNode(String id, String parentId, String name, Double value) {
            super();
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.value = value;
        }
    }

    @SuppressWarnings("serial")
    static List<MyNode> TEST_FLAT_NODES = new ArrayList<MyNode>() {
        {
            add(new MyNode("11", null, "节点11", 0.11d));
            add(new MyNode("12", null, "节点12", 0.22d));
            add(new MyNode("21", "11", "节点21", 1.11d));
            add(new MyNode("22", "11", "节点22", 2.11d));
            add(new MyNode("23", "12", "节点23", 3.11d));
            add(new MyNode("24", "11", "节点24", 4.11d));
            add(new MyNode("31", "24", "节点31", 7.11d));
        }
    };

}
