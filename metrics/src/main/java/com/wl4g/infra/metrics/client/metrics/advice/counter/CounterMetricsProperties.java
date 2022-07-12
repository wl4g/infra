package com.wl4g.infra.metrics.client.metrics.advice.counter;

import org.springframework.context.annotation.Configuration;

/**
 * Counter monitor measure properties.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年6月1日
 * @since
 */
@Configuration
public class CounterMetricsProperties {

    /**
     * An expression of the statistical AOP point cut for the number of calls.
     */
    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String pointcutExpression) {
        if (pointcutExpression == null || pointcutExpression.trim().length() == 0)
            throw new IllegalArgumentException("Counter metrics pointcut expression is null.");

        this.expression = pointcutExpression;
    }

}
