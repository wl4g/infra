# Infra

A enterprise-grade microservice infrastructure framework, The following frameworks have been integrated, support seamless in switching between :

[![Build on Push](https://github.com/wl4g/infra/actions/workflows/build_on_push.yaml/badge.svg)](https://github.com/wl4g/infra/actions/workflows/build_on_push.yaml)
[![Build on Timing](https://github.com/wl4g/infra/actions/workflows/build_on_timing.yaml/badge.svg)](https://github.com/wl4g/infra/actions/workflows/build_on_timing.yaml)
[![License](https://img.shields.io/badge/license-Apache2.0+-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![GraalVM](https://img.shields.io/badge/GraalVM-22.1-green)](https://github.com/wl4g/infra)
[![JVM](https://img.shields.io/badge/JVM-8%20and%2011%2B-green)](https://github.com/wl4g/infra)
[![GithubStars](https://img.shields.io/github/stars/wl4g/infra)](https://github.com/wl4g/infra)

- [**`feign+springboot`**](./integration/integration-feign-core)
- [**`feign+springcloud`**](./integration/integration-feign-springcloud)
- [**`feign+springboot+istio`**](./integration/integration-feign-istio)
- [**`feign+springboot+dubbo`**](./integration/integration-feign-dubbo)

## Directories structure

```
├── infra-bom # dopass-infra Public dependence
├── infra-common # Commonly used utils and helpers, e.g SSH2Holders/SnowflakeIdGenerator/Encodes/, etc
├── infra-common-shade # infra-common Integration package for modules
├── infra-context # Enhancements based on springboot context features, such as support for custom SpringApplication#setDefaultProperties coverage configuration (META-INF/bootstrapping.groovy), built-in request/response body debug log output, integrated opentelemetry tracking, etc.
├── infra-core # Based on the spring boot system features enhancement, such as enhanced spring MVC request version mapping, unified exception handling, framework automatic configuration spring.config.name etc.
├── infra-data # Db/Mybatis related packages, such as mybatis hot loading, multi data sources, etc
├── infra-opencv # The Java version of OpenCV makes it possible to perform visual functions directly in spring applications
│   ├── integration-example # Sample project of distributed architecture based on springboot/cloud
│   ├── integration-feign-common # In order to make the following integration architectures such as springboot+feign、springcloud+feign easy to switch, some common parts such as @HystrixCommand/@FeignClient annotation may be shared.
│   ├── integration-circuitbreaker-hystrix-turbine-server # springcloud+hystrix+turbine Architecture integration and encapsulation
│   ├── integration-feign-core # springboot+feign Architecture integration and encapsulation
│   ├── integration-feign-istio # springboot+istio Architecture integration and encapsulation
│   ├── integration-feign-springcloud # springcloud+feign Architecture integration and encapsulation
│   ├── integration-feign-springcloud-dubbo # springcloud+feign+dubbo Architecture integration and encapsulation
│   ├── integration-feign-springcloud-seata # springcloud+feign+seata Architecture integration and encapsulation
│   ├── integration-springcloud-eureka-server # springcloud+eureka-server Architecture integration and encapsulation
│   └── integration-shardingproxy-server # shardingsphere customized enhanced version, such as supporting failover (master-slave automatic switching), etc
└── infra-support # The common springboot application component encapsulation, such as redisOperator(support for the coexistence of single cluster), distributed command-line device supporting timeout, etc
```

## Stargazers over time

[![Stargazers over time](https://starchart.cc/wl4g/infra.svg)](https://starchart.cc/wl4g/infra)
