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

package com.wl4g.component.common.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.wl4g.component.common.annotation.Todo;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static com.wl4g.component.common.function.TreeConvertor.TreeNode;
import static com.wl4g.component.common.lang.Assert2.notNullOf;

/**
 * 
 * 平面(List)树与children树互转器. {@link TreeConvertor}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-09-08
 * @since
 * @param <T>
 */
@NotThreadSafe
public class TreeConvertor<T extends TreeNode<T>> {
	private final String rootParentId;
	private final NodeIdMatcher matcher;
	private final List<T> subChildrens = new ArrayList<>();
	private final List<T> retList = new ArrayList<>();

	public TreeConvertor() {
		this(null, NodeIdMatcher.defaultInstance);
	}

	/**
	 * 创建树结构转换器
	 * 
	 * @param rootParentId
	 *            跟节点的parentId的值
	 */
	public TreeConvertor(@Nullable String rootParentId, @NotNull NodeIdMatcher matcher) {
		this.rootParentId = rootParentId;
		this.matcher = notNullOf(matcher, "matcher");
	}

	/**
	 * 将平面树格式化为children树
	 * 
	 * @param planeTree
	 *            平面结构的树列表
	 * @param isFilterLowest
	 *            是否过滤(每层)最低级的节点
	 * @return 返回包含children节点关系的树
	 */
	public List<T> formatToChildren(List<T> planeTree, boolean isFilterLowest) {
		// 1.1 parse children tree.
		List<T> childrenTree = new ArrayList<T>();

		for (T n : planeTree) {
			if (n != null && matcher.eq(n.getParentId(), rootParentId)) {
				childrenTree.add(n);
			}

			for (T t : planeTree) {
				if (t != null && n != null && matcher.eq(t.getParentId(), n.getId())) {

					if (emptyChildrens(n)) {
						List<T> childrens = new ArrayList<T>();
						childrens.add(t);
						n.setChildrens(childrens);
					} else
						n.getChildrens().add(t);
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
	 * @param childrenTree
	 *            children结构的树列表
	 * @return 返回不包含children节点关系(以parentId做父子关系)的平面树
	 */
	public List<T> parseChildren(List<T> childrenTree) {

		if (childrenTree != null) {
			for (T n : childrenTree) {
				if (!emptyChildrens(n)) {
					parseChildren(n.getChildrens());
					n.getChildrens().clear();
				}
				retList.add(n);
			}
		}

		return retList;
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
	public List<T> subChildrens(List<T> childrenTree, String pId) {

		if (childrenTree != null) {
			for (T t : childrenTree) {
				if (matcher.eq(t.getId(), String.valueOf(pId)))
					subChildrens.add(t);

				// 继续递归直到找到匹配节点为止.
				subChildrens(t.getChildrens(), pId);
			}
		}

		return subChildrens;
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
	private List<T> childrenLevelSet(T parent, List<T> childrenTree) {

		for (T t : childrenTree) {
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
	private List<T> filterChildren(List<T> childrenTree) {

		Iterator<T> it = childrenTree.iterator();
		while (it.hasNext()) {
			T t = it.next();

			if (!emptyChildrens(t))
				// 继续递归直到找到匹配节点为止.
				filterChildren(t.getChildrens());
			else
				it.remove();
		}

		return childrenTree;
	}

	/**
	 * 是否存在子节点
	 * 
	 * @param t
	 * @return
	 */
	private boolean emptyChildrens(T t) {
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
	private void increaseLevel(T t, int parentLevel) {
		t.setLevel(++parentLevel);
	}

	/**
	 * 子节点递归累加设置
	 * 
	 * @param t
	 * @param parentLevel
	 */
	@Todo
	public List<T> childrenTotalSet(List<T> childrenTree) {
		// TODO
		// for (T t : childrenTree) {
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
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2017-09-08
	 * @since
	 * @param <T>
	 */
	public static interface TreeNode<T> extends Serializable {

		// --- Tree node basic. ---

		String getId();

		void setId(String id);

		String getParentId();

		void setParentId(String parentId);

		int getLevel();

		void setLevel(int level);

		List<T> getChildrens();

		void setChildrens(List<T> childrens);

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

	/**
	 * {@link NodeIdMatcher}
	 *
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2020-11-04
	 * @since
	 */
	public static interface NodeIdMatcher {

		/**
		 * Check nodes ID is equals.
		 * 
		 * @param nodeId1
		 * @param nodeId2
		 * @return
		 */
		boolean eq(String nodeId1, String nodeId2);

		/**
		 * Default {@link NodeIdMatcher} instance of string equals.
		 */
		public static final NodeIdMatcher defaultInstance = (nodeId1, nodeId2) -> StringUtils.equals(nodeId1, nodeId2);

	}

}
