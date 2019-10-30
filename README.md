## Ribbon学习笔记

使用Eureka 一个服务，两个提供服务者。参考Eureka的使用

创建一个Ribbon 客户端：
1.引入Maven依赖
```xml
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-<u>netflix-ribbon</artifactId>
    </dependency>
    <!-- <u>eureka</u> 客户端 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
```
2. 启动类加上 Eureka 客户端注解，注入负载均衡的RestTemplate
```java
@SpringBootApplication
@EnableDiscoveryClient
public class RibbonApplication {
    public static void main(String[] args) {
        SpringApplication.run(RibbonApplication.class, args);
    }   
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```
3,使用RestTemplate 做http 请求。
```java
@RestController
public class RestTemplateController {
    @Autowired
    private RestTemplate restTemplate;
    @GetMapping("/add")
    public String add(Integer a, Integer b) {
        String result = restTemplate.getForObject("http://spring-eureka-producer/produce/hello", String.class);
        return result;
    }
}
```
4.修改配置文件，配置上Eureka 服务地址


两个服务提供者除了端口不一致其他完全一致，通过Ribbon的controller 接口请求，发现服务请求会在不同的服务上轮询。


### Ribbon 负载均衡配置与自定义配置

* 随机策略
* 轮询策略 （Ribbon 默认策略）
* 最低并发策略
* 响应时间加权策略
* 。。。。。等其他

1. 全局策略设置，凡是通过Ribbon的请求都会按照配置的规则进行
```java
@Configuration
public class TestConfiguration{
    @Bean
    public IRule ribbonRule(){
        return new RandomRule() ; // 随机策略
    }
}
```
2.针对某一个源服务设置其特有的策略，
```java
@configuration
@AvoidScan   //注解一个空的声明
public class TestConfiguration{
    @Autowired
    IClientConfig config; //针对客户端的配置管理器
    
    @Bean
    public IRule ribbonRule(IClientConfig config){
        return new RandomRule();
    }
}
```
RibbonClient 配置, 在启动类上加上配置
```java
@RibbonClient(name ="spring-eureka-producer", 
configuration = TestConfiguration.class)
@ComponentScan(excludeFilters= {@ComponentScan.Filter(type = FilterType.ANNOTATION,value ={AvoidScan.class})})
```
name ：spring //标识对于该服务的策略是经过TestConfiguration 所配置的策略。
第二个是，排除扫描被@AvoidScan 注解标记的配置类。


也可以使用配置文件：
```yml
spring-eureka-producer:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
```

### Ribbon 的超时与重试
使用HTTP 发起请求，超时和重试的配置。
```yml
spring-eureka-producer:
  ribbon:
    ConnectTimeout: 30000
    ReadTimeout: 30000
    MaxAutoRetries: 1 
    MaxAutoRetriesNextServer: 1
    OKToRetryOnAllOperations: true
```

### Ribbon 饥饿加载
Ribbon在启动的时候，并不会加载上下文，而是在实际请求的时候才去创建。 我们可以通过指定Ribbon，在启动的时候就加载
```yml
ribbon:
  eager-load:
    enabled: true
    clients: spring-eureka-producer, client-a
```

### Ribbon 脱离Eureka服务
禁用eureka ，配置服务地址
````yml
client:
  ribbon:
    listOfServers: http://localhost:7070, 
```
client 为指定客户端的名称