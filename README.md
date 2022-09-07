# Infra

> An enterprise-grade microservice infrastructure framework, The following frameworks have been integrated, support seamless in switching between :

- [**`feign+springboot`**](./infra-integration/infra-integration-feign-core)
- [**`feign+springcloud`**](./infra-integration/infra-integration-feign-springcloud)
- [**`feign+springboot+istio`**](./infra-integration/infra-integration-feign-istio)
- [**`feign+springboot+dubbo`**](./infra-integration/infra-integration-feign-dubbo)

## Directories structure

```
├── infra-bom # dopass-infra Public dependence
├── infra-common # Commonly used utils and helpers, e.g SSH2Holders/SnowflakeIdGenerator/Encodes/, etc
├── infra-common-shade # infra-common Integration package for modules
├── infra-core # Based on the spring boot system features enhancement, such as enhanced spring MVC request version mapping, unified exception handling, framework automatic configuration spring.config.name etc.
├── infra-data # Db/Mybatis related packages, such as mybatis hot loading, multi data sources, etc
├── infra-opencv # The Java version of OpenCV makes it possible to perform visual functions directly in spring applications
├── infra-rpc # Based on the integration and encapsulation of springboot / cloud distributed architecture, it supports a variety of frameworks, e.g: `springboot-feign/springcloud-feign/springcloud-dubbo/springboot-servicemesh` fast switching and so on
│   ├── infra-integration-example # Sample project of distributed architecture based on springboot/cloud
│   ├── infra-integration-feign-common # In order to make the following integration architectures such as springboot+feign、springcloud+feign easy to switch, some common parts such as @HystrixCommand/@FeignClient annotation may be shared.
│   ├── infra-integration-circuitbreaker-hystrix-turbine-server # springcloud+hystrix+turbine Architecture integration and encapsulation
│   ├── infra-integration-feign-core # springboot+feign Architecture integration and encapsulation
│   ├── infra-integration-feign-istio # springboot+istio Architecture integration and encapsulation
│   ├── infra-integration-feign-springcloud # springcloud+feign Architecture integration and encapsulation
│   ├── infra-integration-feign-springcloud-dubbo # springcloud+feign+dubbo Architecture integration and encapsulation
│   ├── infra-integration-feign-springcloud-seata # springcloud+feign+seata Architecture integration and encapsulation
│   ├── infra-integration-springcloud-eureka-server # springcloud+eureka-server Architecture integration and encapsulation
│   └── infra-integration-shardingproxy-server # shardingsphere customized enhanced version, such as supporting failover (master-slave automatic switching), etc
└── infra-support # The common springboot application component encapsulation, such as redisOperator(support for the coexistence of single cluster), distributed command-line device supporting timeout, etc
```

## Stargazers over time

[![Stargazers over time](https://starchart.cc/wl4g/infra.svg)](https://starchart.cc/wl4g/infra)
