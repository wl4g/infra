package com.wl4g.infra.metrics.client.metrics.advice.timed;

import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.metrics.client.constants.MetricsInfraConstants.CONF_PREFIX_INFRA_TIMED;

import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.CustomLog;

/**
 * {@link TimedAdviceAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-11-19 v1.0.0
 * @since v1.0
 */
@CustomLog
@Configuration
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_TIMED + ".enabled", matchIfMissing = false)
public class TimedAdviceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_TIMED)
    public TimedMetricsProperties timedMetricsProperties() {
        return new TimedMetricsProperties();
    }

    @Bean
    public AspectJExpressionPointcutAdvisor defaultTimingAspectJExpressionPointcutAdvisor(
            TimedMetricsProperties config,
            TimedMetricsAdvice advice) {
        notNull(config.getExpression(), "Expression of the timeouts AOP pointcut is null.");
        log.info("Intializing timing aspectJExpressionPointcutAdvisor. {}", config);
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(config.getExpression());
        advisor.setAdvice(advice);
        return advisor;
    }

    @Bean
    public TimedMetricsAdvice defaultTimingMetricsAdvice() {
        return new TimedMetricsAdvice();
    }

}
