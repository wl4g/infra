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
package com.wl4g.infra.common.bean.page;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtilsBean2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wl4g.infra.common.bridge.RpcContextHolderBridges;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.annotations.ApiParam;

/**
 * The original intention of the integrated paging packaging model is that
 * multiple modules under microservices must be completely decoupled. Therefore,
 * we refer to part of the code of 11 instead of relying on it directly. We are
 * very grateful for {@link com.github.pageSpechelper.PageSpec} work and fully
 * abide by your agreements.
 * 
 * @auhtor James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2018年9月7日
 * @since
 * @see https://github.com/pageSpechelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
 */
@ApiModel("Pagination")
@SuppressWarnings("unchecked")
public class PageHolder<E> implements Serializable {
    private static final long serialVersionUID = -7002775417254397561L;

    /**
     * PageSpec of {@link PageSpec}
     */
    @Schema(hidden = true, accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE)
    @JsonIgnore
    private PageSpec pageSpec;

    /**
     * PageSpec record rows.</br>
     * </br>
     * 
     * <b>Note:</b> The following annotation combination configuration does not
     * implement the effect that the records field can make the request display
     * no two responses. Can't swagger2 still achieve this effect: when the type
     * of request and response are the same model class, can't some fields be
     * displayed or not displayed according to the request and response? </br>
     * </br>
     * 
     * <p>
     * for negative examples:
     * 
     * <pre>
     * &#64;ApiOperation(value = "Query myuser page list")
     * &#64;RequestMapping(value = "/list", method = { GET })
     * public RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; list(PageModel&lt;MyUserModel&gt; pm, MyUserModel param) {
     *     RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; resp = RespBase.create();
     *     resp.setData(myUserService.pageSpec(pm, param));
     *     return resp;
     * }
     * </pre>
     * 
     * for positive examples(Solution):
     * 
     * <pre>
     * &#64;ApiOperation(value = "Query myuser page list")
     * &#64;ApiImplicitParams({
     *	&#64;ApiImplicitParam(name = "pageNum", dataType = "int32", defaultValue = "1"),
     *	&#64;ApiImplicitParam(name = "pageSize", dataType = "int32", defaultValue = "10")
     * })
     * &#64;RequestMapping(value = "/list", method = { GET })
     * public RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; list({@code @ApiIgnore} PageModel&lt;MyUserModel&gt; pm, MyUserModel param) {
     * 	RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; resp = RespBase.create();
     * 	resp.setData(myUserService.pageSpec(pm, param));
     * 	return resp;
     * }
     * </pre>
     * </p>
     */
    @Schema(hidden = false, accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY)
    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private List<E> records = (List<E>) DEFAULT_RECORDS;

    public PageHolder() {
        this(1, 10);
    }

    public PageHolder(@NotNull PageHolder<E> page) {
        notNullOf(page, "page");
        setPageSpec(page.getPageSpec());
    }

    public PageHolder(@NotNull PageSpec pageSpec) {
        setPageSpec(pageSpec);
    }

    public PageHolder(@Nullable Integer pageNum, @Nullable Integer pageSize) {
        setPageSpec(new PageSpec());
        getPageSpec().setPageNum(pageNum);
        getPageSpec().setPageSize(pageSize);
    }

    public PageSpec getPageSpec() {
        return pageSpec;
    }

    public final void setPageSpec(@NotNull PageSpec pageSpec) {
        this.pageSpec = notNullOf(pageSpec, "pageSpec");
    }

    public final PageHolder<E> withPageSpec(@NotNull PageSpec pageSpec) {
        setPageSpec(pageSpec);
        return this;
    }

    public Integer getPageNum() {
        return pageSpec.getPageNum();
    }

    public void setPageNum(@Nullable Integer pageNum) {
        if (nonNull(pageNum)) {
            pageSpec.setPageNum(pageNum);
        }
    }

    public PageHolder<E> withPageNum(@Nullable Integer pageNum) {
        setPageNum(pageNum);
        return this;
    }

    public Integer getPageSize() {
        return pageSpec.getPageSize();
    }

    public void setPageSize(@Nullable Integer pageSize) {
        if (nonNull(pageSize)) {
            pageSpec.setPageSize(pageSize);
        }
    }

    public PageHolder<E> withPageSize(@Nullable Integer pageSize) {
        setPageSize(pageSize);
        return this;
    }

    public Long getTotal() {
        return pageSpec.getTotal();
    }

    public void setTotal(@Nullable Long total) {
        if (nonNull(total)) {
            pageSpec.setTotal(total);
        }
    }

    public PageHolder<E> withTotal(@Nullable Long total) {
        setTotal(total);
        return this;
    }

    public Integer getPages() {
        return pageSpec.getPages();
    }

    public void setPages(Integer pageSpecs) {
        if (nonNull(pageSpecs)) {
            pageSpec.setPages(pageSpecs);
        }
    }

    public PageHolder<E> withPages(Integer pageSpecs) {
        return this;
    }

    public List<E> getRecords() {
        return records;
    }

    public final void setRecords(List<E> records) {
        if (nonNull(records) && !records.isEmpty()) {
            this.records = records;
        }
    }

    public PageHolder<E> withRecords(List<E> records) {
        setRecords(records);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().concat("<").concat(toJSONString(this)).concat(">");
    }

    // --- Current pageSpec function methods. ---

    public PageHolder<E> useCount() {
        getPageSpec().setCount(true);
        return this;
    }

    /**
     * Sets pageSpec in current Rpc context.
     */
    public PageHolder<E> bind() {
        Util.bind(false, getPageSpec());
        return this;
    }

    /**
     * For example jdbc/mybatis/mongo-collection/... pagination specification.
     * </br>
     * Thank refer to very much {@link com.github.pageSpechelper.PageSpec}. We
     * are in full compliance with your agreements.
     * 
     * @param <E>
     * @see http://git.oschina.net/free/Mybatis_PageHelper
     */
    public static class PageSpec implements Serializable {
        private static final long serialVersionUID = -5149671532631896079L;

        /** PageSpec number, starting from 0 */
        private int pageNum;
        /** PageSpec size. */
        private int pageSize;
        /** Start row. */
        private int startRow;
        /** End row. */
        private int endRow;
        /** Total row count. */
        private long total;
        /** Total pageSpec size. */
        private int pageSpecs;
        /** Include count query. */
        private boolean count;
        /**
         * Count signal: in three cases, the default boundsql is executed when
         * null, count is executed when true, and paging is performed when
         * false.
         */
        private Boolean countSignal;
        /** Sort of sql. */
        private String orderBy;
        /** Add sort only. */
        private boolean orderByOnly;
        /** Paging rationalization. */
        private Boolean reasonable;
        /**
         * When set to true, if PageSize is set to 0 (or rowbounds limit = 0),
         * paging is not performed and all results are returned.
         */
        private Boolean pageSizeZero;

        public PageSpec() {
            super();
        }

        public PageSpec(int pageNum, int pageSize) {
            this(pageNum, pageSize, true, null);
        }

        public PageSpec(int pageNum, int pageSize, boolean count) {
            this(pageNum, pageSize, count, null);
        }

        private PageSpec(int pageNum, int pageSize, boolean count, Boolean reasonable) {
            if (pageNum <= 1 && pageSize == Integer.MAX_VALUE) {
                pageSizeZero = true;
                pageSize = 0;
            }
            this.pageNum = (pageNum <= 0 ? 0 : pageNum);
            this.pageSize = pageSize;
            this.count = count;
            calculateStartAndEndRow();
            setReasonable(reasonable);
        }

        /**
         * int[] rowBounds 0 : offset 1 : limit
         */
        public PageSpec(int[] rowBounds, boolean count) {
            if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
                pageSizeZero = true;
                this.pageSize = 0;
            } else {
                this.pageSize = rowBounds[1];
                this.pageNum = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
            }
            this.startRow = rowBounds[0];
            this.count = count;
            this.endRow = this.startRow + rowBounds[1];
        }

        public int getPages() {
            return pageSpecs;
        }

        public PageSpec setPages(int pageSpecs) {
            this.pageSpecs = pageSpecs;
            return this;
        }

        public int getEndRow() {
            return endRow;
        }

        public PageSpec setEndRow(int endRow) {
            this.endRow = endRow;
            return this;
        }

        public int getPageNum() {
            return pageNum;
        }

        public PageSpec setPageNum(int pageNum) {
            // 分页合理化，针对不合理的页码自动处理
            this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public PageSpec setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public int getStartRow() {
            return startRow;
        }

        public PageSpec setStartRow(int startRow) {
            this.startRow = startRow;
            return this;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
            if (total == -1) {
                pageSpecs = 1;
                return;
            }
            if (pageSize > 0) {
                pageSpecs = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
            } else {
                pageSpecs = 0;
            }
            // 分页合理化，针对不合理的页码自动处理
            if ((reasonable != null && reasonable) && pageNum > pageSpecs) {
                pageNum = pageSpecs;
                calculateStartAndEndRow();
            }
        }

        public Boolean getReasonable() {
            return reasonable;
        }

        public PageSpec setReasonable(Boolean reasonable) {
            if (reasonable == null) {
                return this;
            }
            this.reasonable = reasonable;
            // 分页合理化，针对不合理的页码自动处理
            if (this.reasonable && this.pageNum <= 0) {
                this.pageNum = 1;
                calculateStartAndEndRow();
            }
            return this;
        }

        public Boolean getPageSizeZero() {
            return pageSizeZero;
        }

        public PageSpec setPageSizeZero(Boolean pageSizeZero) {
            if (pageSizeZero != null) {
                this.pageSizeZero = pageSizeZero;
            }
            return this;
        }

        /**
         * 计算起止行号
         */
        private void calculateStartAndEndRow() {
            this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
            this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
        }

        public boolean isCount() {
            return this.count;
        }

        public PageSpec setCount(boolean count) {
            this.count = count;
            return this;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public PageSpec setOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public boolean isOrderByOnly() {
            return orderByOnly;
        }

        public void setOrderByOnly(boolean orderByOnly) {
            this.orderByOnly = orderByOnly;
        }

        public Boolean getCountSignal() {
            return countSignal;
        }

        public void setCountSignal(Boolean countSignal) {
            this.countSignal = countSignal;
        }

        // 增加链式调用方法

        /**
         * 设置页码
         *
         * @param pageNum
         * @return
         */
        public PageSpec pageNum(int pageNum) {
            // 分页合理化，针对不合理的页码自动处理
            this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
            return this;
        }

        /**
         * 设置页面大小
         *
         * @param pageSize
         * @return
         */
        public PageSpec pageSize(int pageSize) {
            this.pageSize = pageSize;
            calculateStartAndEndRow();
            return this;
        }

        /**
         * 是否执行count查询
         *
         * @param count
         * @return
         */
        public PageSpec count(Boolean count) {
            this.count = count;
            return this;
        }

        /**
         * 设置合理化
         *
         * @param reasonable
         * @return
         */
        public PageSpec reasonable(Boolean reasonable) {
            setReasonable(reasonable);
            return this;
        }

        /**
         * 当设置为true的时候，如果pageSpecsize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
         *
         * @param pageSizeZero
         * @return
         */
        public PageSpec pageSizeZero(Boolean pageSizeZero) {
            setPageSizeZero(pageSizeZero);
            return this;
        }

        @Override
        public String toString() {
            return "PageSpec{" + "count=" + count + ", pageNum=" + pageNum + ", pageSize=" + pageSize + ", startRow=" + startRow
                    + ", endRow=" + endRow + ", total=" + total + ", pageSpecs=" + pageSpecs + ", countSignal=" + countSignal
                    + ", orderBy='" + orderBy + '\'' + ", orderByOnly=" + orderByOnly + ", reasonable=" + reasonable
                    + ", pageSizeZero=" + pageSizeZero + '}';
        }
    }

    /**
     * Internal utility for {@link PageHolder}
     */
    public static final class Util {

        /**
         * Sets pageSpec in current Rpc context.
         * 
         * @param useServerContext
         * @param pageSpec
         */
        public static void bind(boolean useServerContext, @Nullable PageSpec pageSpec) {
            localCurrentPage.set(pageSpec);
            if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed(cluster)?
                RpcContextHolderBridges.invokeSet(useServerContext, CURRENT_PAGE_KEY, pageSpec);
            }
        }

        /**
         * Gets current {@link PageHolder} from (RPC)context.
         * 
         * @param useServerContext
         * @return
         */
        public @Nullable static <T> PageSpec current(boolean useServerContext) {
            PageSpec lCurrentPage = (PageSpec) localCurrentPage.get();
            if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed-mode?
                PageSpec rCurrentPage = (PageSpec) RpcContextHolderBridges.invokeGet(useServerContext, CURRENT_PAGE_KEY,
                        PageSpec.class);
                if (nonNull(rCurrentPage) && nonNull(lCurrentPage)) {
                    try {
                        BeanUtilsBean2.getInstance().copyProperties(rCurrentPage, lCurrentPage);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                } else { // Fallback
                    lCurrentPage = rCurrentPage;
                }
            }
            return lCurrentPage;
        }

        /**
         * Reload current executed paging information to local current pageSpec
         * object and release from local and origin RPC context.
         */
        public static void update() {
            // Reload executed paging information to local current pageSpec
            // object.
            current(true);

            // Remove object reference from local.
            localCurrentPage.remove();

            // Remove from RPC origin context.
            if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed-mode?
                // It's cleanup too:
                // see:com.wl4g.infra.integration.feign.core.context.interceptor.ProviderFeignContextFilter#postHandle
                RpcContextHolderBridges.invokeRemoveAttachment(false, CURRENT_PAGE_KEY);
            }
        }

        /**
         * Distributed mode, current pageSpec RPC context key.
         */
        private static transient final String CURRENT_PAGE_KEY = "currentPage";
        private static transient final ThreadLocal<PageSpec> localCurrentPage = new ThreadLocal<>();
    }

    private static transient final List<?> DEFAULT_RECORDS = unmodifiableList(emptyList());
}