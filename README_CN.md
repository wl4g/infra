# Infra

> 企业级微服务基础架构框架，已整合以下框架，支持在以下框架之间无缝切换:

- [**`feign+springboot`**](./integration/integration-feign-core)
- [**`feign+springcloud`**](./integration/integration-feign-springcloud)
- [**`feign+springboot+istio`**](./integration/integration-feign-istio)
- [**`feign+springboot+dubbo`**](./integration/integration-feign-dubbo)

## 目录说明

```bash
├── infra-bom # dopass-infra公共依赖
├── infra-common # 常用Utils、Helpers等, 如 SSH2Holders/SnowflakeIdGenerator/Encodes/ClassPathResourcePatternResolver等
├── infra-common-shade # infra-common模块的集成包
├── infra-context # 基于springboot context 特性增强，如支持自定义SpringApplication#setDefaultProperties覆盖配置(META-INF/bootstrapping.groovy)、内置request/response body调试日志输出、集成opentelemetry追踪等
├── infra-core # 基于springboot体系特性增强, 如增强springMvc请求版本映射、统一异常处理、框架式自动配置spring.config.name等
├── infra-data # DB/Mybatis相关封装, 如mybatis热加载、多数据源等
├── infra-opencv # opencv的java版封装, 使在spring应用直接执行视觉功能任务成为可能
│   ├── integration-example # 基于springboot/cloud分布式架构示例项目
│   ├── integration-feign-common # 为使如下springboot+feign、springcloud+feign等不同整合架构可轻易切换,可能会共用一些如@HystrixCommand/@FeignClient注解等公共部分
│   ├── integration-circuitbreaker-hystrix-turbine-server # springcloud+hystrix+turbine整合封装
│   ├── integration-feign-core # springboot+feign架构整合封装
│   ├── integration-feign-istio # springboot+istio架构整合封装
│   ├── integration-feign-springcloud # springcloud+feign架构整合封装
│   ├── integration-feign-springcloud-dubbo # springcloud+feign+dubbo架构整合封装
│   ├── integration-feign-springcloud-seata # springcloud+feign+seata架构整合封装
│   ├── integration-regcenter-eureka-server # springcloud+eureka-server架构整合封装
│   └── integration-shardingproxy-server # shardingsphere 定制增强版，如:支持故障转移(主从自动切换)等
└── infra-support # 常用springboot应用组件封装, 如redisOperator(支持单机集群并存)、支持超时的分布式命令行器等
```

## Stargazers over time

[![Stargazers over time](https://starchart.cc/wl4g/infra.svg)](https://starchart.cc/wl4g/infra)
