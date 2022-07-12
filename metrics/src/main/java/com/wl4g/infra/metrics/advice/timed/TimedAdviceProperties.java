package com.wl4g.infra.metrics.advice.timed;

/**
 * 
 * {@link TimedAdviceProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-11-19 v1.0.0
 * @since v1.0
 */
public class TimedAdviceProperties {

    /**
     * Call time consuming AOP point cut surface expression.
     */
    private String expression;

    public void setExpression(String pointcutExpression) {
        if (pointcutExpression == null || pointcutExpression.trim().length() == 0)
            throw new IllegalArgumentException("Timer metrics pointcut expression is null.");
        this.expression = pointcutExpression;
    }

    public String getExpression() {
        return expression;
    }

}