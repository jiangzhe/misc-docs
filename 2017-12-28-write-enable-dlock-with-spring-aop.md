---
layout: post
title: "使用Spring AOP写一个简单的分布式锁工具"
date: 2017-12-28 23:05:00 +0800
categories: Spring
permalink: /:categories/:title
---

- 背景

最近用Spring Boot框架开发Web服务，打包成镜像放在Kubernates集群上运行。因为希望保证HA，所以采用多replica的模式，但遇到了一点小问题。服务本身有少量业务流程要求必须同一时间只有一个执行，另外还有部分定点启动的后台任务，需要避免重复调度。因此，需要有一个多服务间的同步工具。
这样的功能可以用ZooKeeper实现，不过考虑到引入ZooKeeper有点重，觉得基于数据库做个简单的就行。另外，团队开发的其他服务也有类似的需求，所以也希望能够通用些，最好不用其他人再重复造轮子了。
背景大概便是这样。

- 最初的设计

1. 核心是一张Lock表，每一条记录就是一个锁。
2. 通过MySQL的select ... where pk = ? for update语句获得行级锁，完成任务后释放锁。
3. 提供一个接口如下，用户通过lambda传递获得锁以后需要执行的行为。

```java
public interface DLockExecutor {
    void tryLock(DLockType type,
                 int waitTimeInSeconds,
                 Function<DLock, LockAttributes> mayAcquired);
}
```

其中，waitTimeInSeconds是获取锁时的等待时间，以方便指定超时时间，返回错误信息。

经过一些考虑之后，觉得这样的设计有一些毛病。
首先，通过for update锁行以后，其他进程阻塞等待其实并不友好，不如改进为直接返回让用户决定
是否重试或是直接放弃。
另外，接口中的lambda的含义不很清晰，而且无法直接返回用户行为结果，需要在外部包装一下，
使用起来不方便。如果能够类似Spring @Scheduled, @Cacheable这样的注解使用，会更加简洁方便。

- 修改后的设计

1. 不使用for update语句来锁表，而直接使用类似CAS操作的SQL来实现乐观锁（但不自旋）。

```sql
update distributed_lock set locked = false, token = ? where pk = ? and locked = false;
select * from distributed_lock where pk = ? and token = ?;
```

这样，即便多个进程同时执行这段SQL抢夺锁，总会有一个先抢到。
而剩余的会很快返回（没有拿到锁），然后是否重试或是直接返回都是由用户确定。

2. 提供一个锁过期时间，来保证如果有进程没有正确地释放锁，其他进程仍有机会在锁的时间失效后再次获得锁。

3. 增加一个LockContext，用来存储Lock相关信息，同时也方便采用注解的形式来简化开发。接口如下：

```java
public interface DLockExecutor {
    void tryLock(DLockType type,
                 int expireTimeInSeconds,
                 Consumer<DLockContext> mayAcquired);
}

public interface DLockContext {
    DLock getDLock();

    LockAttributes findLockAttributes();

    void setLockAttributes(LockAttributes attributes);

    long getCurrentDbTimeMillis();
}
```

LockAttributes保存上一次成功执行时用户设置的信息。例如，可以方便地使用这个信息来使得定时调度任务有条件地跳过。

4. 提供注解@LockAcquired, @LockMayAcquired。当使用注解时，只需要从DLockContext获取和更新信息即可。
甚至在简单场景下，只需要添加注解，源代码不需要做任何改动。

- 实现

1. 因为团队里统一使用了MyBatis最为ORM框架，配置文件和参数都有约定。而我这个功能只涉及很简单的操作，实体类也不复杂，所以决定直接JdbcTemplate。
2. DLockContext可以利用ThreadLocal来实现。在DLockExecutor尝试获取锁后，将结果设置进context，调用用户方法，从context获取用户结果，再持久化到数据库。
3. AOP方面，Spring提供了丰富的抽象类和工具类，所以要实现Advisor, Pointcut都十分方便。
可以看一下AbstractPointcutAdvisor，AnnotationMatchingPointcut，MethodInterceptor。以及借鉴以下Spring cache, scheduling模块里的相应实现。
通过AnnotationMatchingPointcut对被特定注解的方法生成Pointcut；
通过BeanPostProcessor扫描所有Bean方法注解，验证有效性，并构建方法与锁属性的映射，设置到interceptor上以保证运行时会根据不同的锁属性做相应的行为；
通过ApplicationListener对Lock表进行初始化和检验。
4. 使用@ConditionalOnMissingBean可以提供更有好的自动配置。在测试中遇到了多个DataSource导致自动绑定失败，我是增加一个DataSourceProvider接口指定唯一的DataSource来解决这个问题。
