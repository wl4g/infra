# Infra

> An enterprise-grade microservice infrastructure framework, The following frameworks have been integrated, support seamless in switching between :

- [**`feign+springboot`**](./dopaas-infra-integration/dopaas-infra-integration-feign-core)
- [**`feign+springcloud`**](./dopaas-infra-integration/dopaas-infra-integration-feign-springcloud)
- [**`feign+springboot+istio`**](./dopaas-infra-integration/dopaas-infra-integration-feign-istio)
- [**`feign+springboot+dubbo`**](./dopaas-infra-integration/dopaas-infra-integration-feign-dubbo)

## Directories structure

```
├── dopaas-infra-bom # dopass-infra Public dependence
├── dopaas-infra-common # Commonly used utils and helpers, e.g SSH2Holders/SnowflakeIdGenerator/Encodes/, etc
├── dopaas-infra-common-shade # dopaas-infra-common Integration package for modules
├── dopaas-infra-core # Based on the spring boot system features enhancement, such as enhanced spring MVC request version mapping, unified exception handling, framework automatic configuration spring.config.name etc.
├── dopaas-infra-data # Db/Mybatis related packages, such as mybatis hot loading, multi data sources, etc
├── dopaas-infra-opencv # The Java version of OpenCV makes it possible to perform visual functions directly in spring applications
├── dopaas-infra-rpc # Based on the integration and encapsulation of springboot / cloud distributed architecture, it supports a variety of frameworks, e.g: `springboot-feign/springcloud-feign/springcloud-dubbo/springboot-servicemesh` fast switching and so on
│   ├── dopaas-infra-integration-example # Sample project of distributed architecture based on springboot/cloud
│   ├── dopaas-infra-integration-feign-common # In order to make the following integration architectures such as springboot+feign、springcloud+feign easy to switch, some common parts such as @HystrixCommand/@FeignClient annotation may be shared.
│   ├── dopaas-infra-integration-circuitbreaker-hystrix-turbine-server # springcloud+hystrix+turbine Architecture integration and encapsulation
│   ├── dopaas-infra-integration-feign-core # springboot+feign Architecture integration and encapsulation
│   ├── dopaas-infra-integration-feign-istio # springboot+istio Architecture integration and encapsulation
│   ├── dopaas-infra-integration-feign-springcloud # springcloud+feign Architecture integration and encapsulation
│   ├── dopaas-infra-integration-feign-springcloud-dubbo # springcloud+feign+dubbo Architecture integration and encapsulation
│   ├── dopaas-infra-integration-feign-springcloud-seata # springcloud+feign+seata Architecture integration and encapsulation
│   ├── dopaas-infra-integration-springcloud-eureka-server # springcloud+eureka-server Architecture integration and encapsulation
│   └── dopaas-infra-integration-shardingproxy-server # shardingsphere customized enhanced version, such as supporting failover (master-slave automatic switching), etc
└── dopaas-infra-support # The common springboot application component encapsulation, such as redisOperator(support for the coexistence of single cluster), distributed command-line device supporting timeout, etc
```

## Stargazers over time

[![Stargazers over time](https://starchart.cc/wl4g/dopaas-infra.svg)](https://starchart.cc/wl4g/dopaas-infra)
