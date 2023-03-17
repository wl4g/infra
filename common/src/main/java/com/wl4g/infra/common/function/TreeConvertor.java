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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.wl4g.infra.common.annotation.Todo;
import com.wl4g.infra.common.function.TreeConvertor.TreeNode;

/**
 * 
 * 平面(List)树与children树互转器. {@link TreeConvertor}
 *
 * @author James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2017-09-08
 * @since
 * @param <E>
 */
@NotThreadSafe
public class TreeConvertor<ID, E extends TreeNode<ID, E>> {
    private final ID rootParentId;
    private final NodeIdMatcher<ID> matcher;
    private final List<E> childrens = new ArrayList<>();
    private final List<E> flatNodes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public TreeConvertor() {
        this(null, (NodeIdMatcher<ID>) NodeIdMatcher.DEFAULT);
    }

    public TreeConvertor(@Nullable ID rootParentId, @NotNull NodeIdMatcher<ID> matcher) {
        this.rootParentId = rootParentId;
        this.matcher = notNullOf(matcher, "matcher");
    }

    /**
     * 将平面树格式化为children树
     * 
     * @param flatTree
     *            平面结构的树列表
     * @param isFilterLowest
     *            是否过滤(每层)最低级的节点
     * @return 返回包含children节点关系的树
     */
    public List<E> formatToChildren(List<E> flatTree, boolean isFilterLowest) {
        // 1.1 parse children tree.
        List<E> childrenTree = new ArrayList<E>(flatTree.size() / 2);

        for (E n : flatTree) {
            if (n != null && matcher.eq(n.getParentId(), rootParentId)) {
                childrenTree.add(n);
            }
            for (E t : flatTree) {
                if (nonNull(t) && nonNull(n) && matcher.eq(t.getParentId(), n.getId())) {
                    if (emptyChildrens(n)) {
                        List<E> childrens = new ArrayList<E>();
                        childrens.add(t);
                        n.setChildrens(childrens);
                    } else {
                        n.getChildrens().add(t);
                    }
                }
            }
        }

        // 2.1 filter children.
        childrenTree = isFilterLowest ? filterChildren(childrenTree) : childrenTree;

        // 2.2 recursion level/total set.
        return childrenLevelSet(null, childrenTree);
    }

    /**
     * 将children解析为平面树
     * 
     * @param treeNodes
     *            children结构的树列表
     * @return 返回不包含children节点关系(以parentId做父子关系)的平面树
     */
    public List<E> parseChildren(List<E> treeNodes) {
        if (treeNodes != null) {
            for (E n : treeNodes) {
                if (!emptyChildrens(n)) {
                    parseChildren(n.getChildrens());
                    n.getChildrens().clear();
                }
                flatNodes.add(n);
            }
        }
        return flatNodes;
    }

    /**
     * 依据父ID获取所有子、孙等节点列表
     * 
     * @param childrenTree
     *            children树列表
     * @param pId
     *            目标父级ID
     * @return 返回包含pId以及所有子、孙节点
     */
    public List<E> subChildrens(List<E> childrenTree, ID parentId) {
        if (childrenTree != null) {
            for (E t : childrenTree) {
                if (matcher.eq(t.getId(), parentId)) {
                    childrens.add(t);
                }
                // 继续递归直到找到匹配节点为止.
                subChildrens(t.getChildrens(), parentId);
            }
        }
        return childrens;
    }

    /**
     * 子节点递归level设置
     * 
     * @param parent
     *            父节点
     * @param childrenTree
     *            对应子节点列表
     * @return
     * @sine
     */
    private List<E> childrenLevelSet(E parent, List<E> childrenTree) {
        for (E t : childrenTree) {
            if (matcher.eq(t.getParentId(), rootParentId)) {
                t.setLevel(0);
            } else if (parent != null) {
                increaseLevel(t, parent.getLevel());
            }
            if (!emptyChildrens(t)) {
                // 继续递归下级节点.
                childrenLevelSet(t, t.getChildrens());
            }
        }
        return childrenTree;
    }

    /**
     * 递归过滤最底层节点<br/>
     * 注: List做删除时不能直接用list.get(i).remove(object);
     * http://www.cnblogs.com/zhangfei/p/4510584.html
     * 
     * @param childrenTree
     * @return
     */
    private List<E> filterChildren(List<E> childrenTree) {
        final Iterator<E> it = childrenTree.iterator();
        while (it.hasNext()) {
            final E t = it.next();
            if (!emptyChildrens(t)) {
                // 继续递归直到找到匹配节点为止.
                filterChildren(t.getChildrens());
            } else {
                it.remove();
            }
        }
        return childrenTree;
    }

    /**
     * 是否存在子节点
     * 
     * @param t
     * @return
     */
    private boolean emptyChildrens(E t) {
        return (t.getChildrens() == null || t.getChildrens().isEmpty());
    }

    /**
     * 增加设置节点level
     * 
     * @param t
     *            目标节点
     * @param parentLevel
     *            父节点级别
     */
    private void increaseLevel(E t, int parentLevel) {
        t.setLevel(++parentLevel);
    }

    /**
     * 子节点递归累加设置
     * 
     * @param t
     * @param parentLevel
     */
    @Todo
    public List<E> childrenTotalSet(List<E> childrenTree) {
        // TODO
        // for (E t : childrenTree) {
        // if (matcher.eq(t.getParentId(), rootParentId)) {
        // // Map<String, Integer> nodeSubs = new HashMap<String,
        // // Integer>();
        // t.setSum(0);
        // }
        // if (!emptyChildrens(t)) {
        // // 继续递归下级节点.
        // childrenTotalSet(t, t.getChildrens());
        // }
        // }
        return childrenTree;
    }

    /**
     * TreeConvert转换器节点操作接口. {@link TreeNode}
     *
     * @author James Wong <jameswong1376@gmail.com>>
     * @version v1.0 2017-09-08
     * @since
     * @param <E>
     */
    public static interface TreeNode<ID, E> extends Serializable {
        // --- Tree node basic. ---

        ID getId();

        void setId(ID id);

        ID getParentId();

        void setParentId(ID parentId);

        int getLevel();

        void setLevel(int level);

        List<E> getChildrens();

        void setChildrens(List<E> childrens);

        // --- Node statistics. ---

        default int getCount() {
            // Ignore
            return -1;
        }

        default void setCount(int count) {
            // Ignore
        }

        default Double getSum() {
            // Ignore
            return null;
        }

        default void setSum(Double sum) {
            // Ignore
        }

        default Double getValue() {
            // Ignore
            return null;
        }

        default void setValue(Double data) {
            // Ignore
        }
    }

    public static interface NodeIdMatcher<ID> {

        /**
         * Check nodes ID is equals.
         * 
         * @param nodeId1
         * @param nodeId2
         * @return
         */
        boolean eq(ID nodeId1, ID nodeId2);

        /**
         * Default {@link NodeIdMatcher} instance of string equals.
         */
        public static final NodeIdMatcher<Object> DEFAULT = (id1, id2) -> StringUtils.equals(valueOf(id1).toString(),
                valueOf(id2).toString());
    }

}
