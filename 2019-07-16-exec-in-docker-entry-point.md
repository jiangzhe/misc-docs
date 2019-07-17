# EXEC在Docker启动命令中的作用

最近在K8S+Docker环境上搭建SpringCloud应用平台，使用了Eureka作为注册中心，部署了基于GIT（GOGS）的配置中心，SpringBootAdmin作为管理中心，Zuul做网关。

每个上架的应用引入eureka starter的包，会在启动时注册到Eureka，在退出前注销。
但在将应用容器化以后，遇到了一个奇怪的问题。

在K8S杀掉POD时，应用没有去Eureka上注销自身服务，导致Eureka上服务状态延迟，进而导致ZUUL网关访问到无效的服务地址。
在本地却没有复现这个问题。

通过一番排查后，发现是容器的启动脚本存在问题。
在本地IDE里，是直接通过IntelliJ来启动应用，用Ctrl+C来结束应用。一切工作正常。

而在容器中，我写了一个Shell脚本，做了一些初始化工作，然后使用java -jar命令来启动Java进程。正是这个地方存在漏洞。

Docker本质上是使用了Namespace和cgroup技术的进程，启动脚本正是初始进程，PID=1。K8S停止POD会向该进程发送TERM信号。
在没有额外设置信号传递的情况下，TERM信号并不会传递给子进程。简单而言，Java进程没有收到TERM信号，所以不会退出。

而K8S有一个超时等待并强制清除的机制，当POD进入TERMINATING状态却一直不退出直到超时后，K8S将强制终止该POD的所有进程。在这时，JVM都被强制终止了，还如何去Eureka上注销服务呢？

解决办法也比较简单：
1. 使用shell内置的exec命令，将java命令替换为当前进程。
```shell
# setup and init 
# ...
# replace current process with java
exec java -jar xxx.jar 
```

2. 使用trap和kill将信号传递给子进程
```shell
_term() {
  kill -TERM "$child" 2>/dev/null
}
trap _term SIGTERM
java -jar xxx.jar &
child=$!
wait "$child"
```
