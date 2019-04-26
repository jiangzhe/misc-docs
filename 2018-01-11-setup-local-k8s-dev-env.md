---
layout: post
title: "构建本地的K8S开发环境"
date: 2018-01-11 07:11:00 +0800
categories: kubernetes
permalink: /:categories/:title
---

最近想研究一下service mesh，刚好看到[conduit](https://github.com/runconduit/conduit)项目，就准备搭个环境玩一玩。

conduit的官方介绍推荐用minikube来构建环境，试了一下，没成。我是用的win7，vmware虚拟的CentOS7环境，docker版本17.06-ce。启动minikube使用参数--vm-driver=none，结果kube-dns一直起不起来。搜了一些issue看，有的说版本问题，有的说是dns解析设置问题，尝试数次未果（网络方面知识缺乏，需要恶补）。

狠狠心，不用minikube了，直接上kubeadm。安装还是挺顺利的，把几个小坑填了，集群就搭起来了。在此记一下安装过程。
以下命令都是在su root后使用的，就不使用sudo了。

根据[官网指南](https://docs.docker.com/engine/installation/linux/docker-ce/centos/)安装docker。

```bash
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install docker-ce
```

需要注意，docker配置项有一处修改：

```
cat << EOF > /etc/docker/daemon.json
{
  "exec-opts": ["native.cgroupdriver=cgroupfs"]
}
EOF
```

然后激活和启动docker服务。

```bash
systemctl daemon-reload
systemctl enable docker && systemctl start docker
```

根据[官网指南](https://kubernetes.io/docs/setup/independent/install-kubeadm/)下载kubeadm。

```bash
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
setenforce 0
yum install -y kubelet kubeadm kubectl
```

事实上，由于墙的问题，导致无法连接google的repo，可以用vpn或代理解决。
我使用了shadowsocks代理。本地启动客户端后对yum进行配置。

```bash
cat << EOF > /etc/yum.conf
# add proxy
proxy=192.168.88.1:1080
EOF
```

安装完以后可以去掉代理配置和禁止kubernetes repo，以免影响其他应用安装升级。

确保kubelet也使用cgroupfs。

```bash
cat << EOF > /etc/systemd/system/kubelet.service.d/05-custom.conf
[Service]
Environment="KUBELET_EXTRA_ARGS=--cgroup-driver=cgroupfs"
EOF
```

激活和启动kubelet服务。

```bash
systemctl daemon-reload
systemctl enable kubelet && systemctl start kubelet
```

根据[官网指南](https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/)创建集群。

因为是root，可以直接配置KUBECONFIG。

```bash
export KUBECONFIG=/etc/kubernetes/admin.conf
```

解除防火墙。

```bash
systemctl stop firewalld
```

禁止swap。

```bash
swapoff -a
```

全局翻墙，并设置例外。

```bash
export http_proxy=192.168.88.1:1080
export https_proxy=192.168.88.1:1080
export no_proxy=`echo 192.168.88.{1..255} | sed 's/ /,/g'`
export no_proxy=$no_proxy,`echo 10.244.0.{1..255} | sed 's/ /,/g'`
export no_proxy=$no_proxy,`echo 10.96.0.{1..255} | sed 's/ /,/g'`
export no_proxy=$no_proxy,127.0.0.1,localhost
```

初始化集群，使用了flannel作为kubernetes的网络管理插件，所以需要添加额外参数指定cidr。

```bash
kubeadm init --pod-network-cidr=10.244.0.0/16
```

集群正常启动后，添加flannel服务。

```bash
kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/v0.9.1/Documentation/kube-flannel.yml
```

查看服务情况，在flannel启动后，kube-dns也会很快从Pending状态转换到Running。

```bash
kubectl get all -n kube-system
```

去除master污点，允许pod在master上启动。

```bash
kubectl taint nodes --all node-role.kubernetes.io/master-
```

然后就可以开始玩耍了。
