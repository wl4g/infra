# Spring Boot Feign Integration

A better feign client library to combine with `SpringBoot`, it can be used for migration 
from the bloated `SpringCloud` architecture to the `SpringBoot` + [istio](https://istio.io) architecture.


---


Write [Feign](https://github.com/OpenFeign/feign) client with `annotation`, like this:


On the basis of `@SpringBootApplication`:

```java
@EnableFeignConsumers(basePackages = "com.wl4g.infra.integration.feign.core")
@SpringBootApplication
public class SpringBootTests {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
```

#### Case1:

We can provide an interface using `@RequestLine` annotations.
##### Note: that it must be configured to `Contract.Default` (default by `SpringMvcContract`)

```java
@FeignConsumer(name = "github", url = "${github.api.url}", configuration = { Contract.Default.class })
public interface GithubService1 {

    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<GitHubContributor> getContributors(@Param("owner") String owner, @Param("repo") String repo);
}
```

Now we can use it as we normally use `Spring`.

```java
    @Autowired
    private GithubService1 githubService1;
    
    List<GitHubContributor> contributors = githubService1.getContributors("wl4g", "infras");
    logger.info("contributors={}", new Gson().toJson(contributors));    
```

#### Case2:

We can provide an interface using `@RequestMapping` annotations. 

```java
@FeignConsumer(name = "github", path = "${github.api.user-path}")
public interface GithubService2 {

    @RequestMapping(method = GET, path = "/users/{owner}/repos")
    List<GitHubRepo> getRepos(@PathVariable("owner") String owner);

```

Now we can use it as we normally use `Spring`.

```java
    @Autowired
    private GithubService2 githubService2;

    List<GitHubRepo> repos = githubService2.getRepos("wl4g");
    logger.info("repos={}", new Gson().toJson(repos));    
```

#### Case3:

Using `@FeignClient` and `@FeignConsumer` is equivalent, and the effect is the same. 
This support is for architecture migration from `Spring Cloud + Feign` to `Spring Boot + Feign + Istio`

```java
@FeignClient(name = "github", path = "${github.api.user-path}abcd1234") // 'path' invalid, 
@RequestMapping("${github.api.user-path}") // Priority, covered @FeignClient#path
public interface GithubService2 {

    @RequestMapping(method = GET, path = "/{owner}/repos")
    List<GitHubRepoModel> getRepos(@PathVariable("owner") String owner);

```

Now we can use it as we normally use `Spring`.

```java
    @Autowired
    private GithubService3 githubService3;

    List<GitHubRepoModel> repos = githubService3.getRepos("wl4g");
    log.info("repos={}", new Gson().toJson(repos));
```

##### For example codes refer to: [src/test/com/wl4g/component/rpc/feign/core/SpringBootFeignTests.java](src/test/com/wl4g/component/rpc/springboot/feign/SpringBootFeignTests.java)


## Features & Description:
- You can also easily pan to the spring cloud feign environment without modifying the annotation, 
because `@FeignConsumer` is compatible with `@FeignClient`

- The classes under package `/netflix/hystrix`&nbsp;,&nbsp;`/feign/hystrix`&nbsp;,&nbsp;`/org/springframework` are from the corresponding official source code. 
The purpose is to be compatible with the migration from `Spring Cloud + Feign` to `Spring Boot + Feign + Istio`. No error will be reported



## More configuration

```yaml
spring:
  infra:
    rpc:
      feign:
        core:
          default-url: https://api.github.com
          default-log-level: BASIC # NONE|BASIC|HEADERS|FULL
          client-provider: okhttp3 # http2Client|okhttp3, Default: okhttp3
          max-idle-connections: 520
          connect-timeout: 11000
          read-timeout: 12000
```