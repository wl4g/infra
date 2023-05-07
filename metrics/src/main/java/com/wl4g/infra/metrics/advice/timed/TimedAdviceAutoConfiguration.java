package com.wl4g.infra.metrics.advice.timed;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.metrics.constants.MetricsInfraConstants.CONF_PREFIX_INFRA_TIMED;

import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.infra.metrics.health.timed.TimedHealthIndicator;

import lombok.CustomLog;

/**
 * {@link TimedAdviceAutoConfiguration}
 * 
 * @author &lt;James Wong James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-11-19 v1.0.0
 * @since v1.0
 */
@SuppressWarnings("deprecation")
@CustomLog
@Configuration
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_TIMED + ".enabled", matchIfMissing = false)
public class TimedAdviceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_TIMED)
    public TimedAdviceProperties timedAdviceProperties() {
        return new TimedAdviceProperties();
    }

    @Bean
    public AspectJExpressionPointcutAdvisor timedAspectJExpressionPointcutAdvisor(
            TimedAdviceProperties config,
            TimedAdvice advice) {
        hasText(config.getExpression(), "The timed advice pointcut expression '{}' is required.",
                CONF_PREFIX_INFRA_TIMED + ".expression");
        log.info("Intializing timed metrics advice. - {}", config);
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(config.getExpression());
        advisor.setAdvice(advice);
        return advisor;
    }

    @Bean
    public TimedAdvice timedAdvice(@Autowired(required = false) TimedHealthIndicator timedIndicator) {
        return new TimedAdvice(timedIndicator);
    }

}
