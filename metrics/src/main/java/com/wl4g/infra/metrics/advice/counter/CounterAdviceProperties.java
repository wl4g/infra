package com.wl4g.infra.metrics.advice.counter;

import org.springframework.context.annotation.Configuration;

/**
 * Counter monitor measure properties.
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0
 * @date 2018年6月1日
 * @since
 */
@Configuration
public class CounterAdviceProperties {

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
