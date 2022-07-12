package com.wl4g.infra.metrics.client.metrics.advice.counter;

import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.metrics.client.constants.MetricsInfraConstants.CONF_PREFIX_INFRA_COUNTER;

import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.CustomLog;

/**
 * Starts bootstrap configuration <br/>
 * Precondition: <br/>
 * `@ConditionalOnBean(MonitorMetricsConfiguration.class)`<br/>
 * DI container must have MonitorMetricsConfiguration objects.<br/>
 * `@AutoConfigureBefore(MonitorMetricsConfiguration.class)`<br/>
 * MonitorMetricsConfiguration objects must be created before that.<br/>
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年5月31日
 * @since
 */
@CustomLog
@Configuration
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_COUNTER + ".enabled", matchIfMissing = false)
public class CounterAdviceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_COUNTER)
    public CounterAdviceAutoConfiguration counterAdviceAutoConfiguration() {
        return new CounterAdviceAutoConfiguration();
    }

    @Bean
    public AspectJExpressionPointcutAdvisor counterAspectJExpressionPointcutAdvisor(
            CounterMetricsProperties props,
            CounterMetricsAdvice advice) {
        notNull(props.getExpression(), "Expression of the counter AOP pointcut is null.");
        log.info("Initial counterAspectJExpressionPointcutAdvisor. {}", props);
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(props.getExpression());
        advisor.setAdvice(advice);
        return advisor;
    }

    @Bean
    public CounterMetricsAdvice counterPerformanceAdvice() {
        return new CounterMetricsAdvice();
    }

}
