# 一个基于Spring Boot/Cloud企业级通用组件。

### 目录说明

```
├── dopaas-infra-bom # dopass-infra公共依赖
├── dopaas-infra-common # 常用Utils、Helpers等, 如 SSH2Holders/SnowflakeIdGenerator/Encodes/ClassPathResourcePatternResolver等
├── dopaas-infra-common-shade # dopaas-infra-common模块的集成包
├── dopaas-infra-core # 基于springboot体系特性增强, 如增强springMvc请求版本映射、统一异常处理、框架式自动配置spring.config.name等
├── dopaas-infra-data # DB/Mybatis相关封装, 如mybatis热加载、多数据源等
├── dopaas-infra-opencv # opencv的java版封装, 使在spring应用直接执行视觉功能任务成为可能
├── dopaas-infra-rpc # 基于springboot/cloud分布式架构的集成与封装, 支持多种框架如dubbo/springcloud-feign/springboot-servicemesh快速切换等
│   ├── dopaas-infra-integration-example # 基于springboot/cloud分布式架构示例项目
│   ├── dopaas-infra-integration-feign-common # 为使如下springboot+feign、springcloud+feign等不同整合架构可轻易切换,可能会共用一些如@HystrixCommand/@FeignClient注解等公共部分
│   ├── dopaas-infra-integration-circuitbreaker-hystrix-turbine-server # springcloud+hystrix+turbine整合封装
│   ├── dopaas-infra-integration-feign-core # springboot+feign架构整合封装
│   ├── dopaas-infra-integration-feign-istio # springboot+istio架构整合封装
│   ├── dopaas-infra-integration-feign-springcloud # springcloud+feign架构整合封装
│   ├── dopaas-infra-integration-feign-springcloud-dubbo # springcloud+feign+dubbo架构整合封装
│   ├── dopaas-infra-integration-feign-springcloud-seata # springcloud+feign+seata架构整合封装
│   ├── dopaas-infra-integration-regcenter-eureka-server # springcloud+eureka-server架构整合封装
│   └── dopaas-infra-integration-shardingproxy-server # shardingsphere 定制增强版，如:支持故障转移(主从自动切换)等
└── dopaas-infra-support # 常用springboot应用组件封装, 如redisOperator(支持单机集群并存)、支持超时的分布式命令行器等
```

