# Patterns in Spring Framework

## 一个Spring Boot Web Server的启动

- application.yml / application.properties 是在何时通过什么方式加载的？
- 补充：为什么要用这种方式？



- @Value, @PropertySource, @ConfigurationProperties


## SpringFactoriesLoader

## Annotation一览

- @Controller, @Service, @Component, @Repository

- @RequestMapping

- @Scheduled, @Cacheable

- @Transactional

## MVC


## Security Pattern


## Starter Pattern

## Lifecycle of application and beans

- InitializingBean, DisposableBean

- ApplicationListener

- @PostConstruct

- SmartInitializingSingleton

- ApplicationRunListener



1. Spring使用了几种日志库，是如何处理他们之间的关系的，在启动时如何保证日志配置的正确加载？

2. Spring使用的是哪个ClassLoader，是在什么时候切换的？为什么要使用一个特定的ClassLoader？

3. 依赖注入有几种方式？分别是如何实现的？适用/不适用于哪些场景？

4. HttpServletRequest和HttpServletResponse是通过什么方式注入到Controller的方法中的？

5. Spring为什么要实现自己的类读取器MetadataReader，直接使用Java的反射存在什么问题？

ConfigurationClassParser.retrieveBeanMethodMetadata()

6. AutoConfiguration究竟是通过什么机制定义Bean的？@ConditionalOnMissingBean实在什么时候判定的？

7. 如何写一个spring boot starter

@Enable*, @Import, ImportAware, spring.factories

8. BeanFactory.getBean()究竟做了什么？
