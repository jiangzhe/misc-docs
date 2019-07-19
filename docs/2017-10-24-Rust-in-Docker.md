---
layout: post
title:  "Rust in Docker"
date:   2017-10-24 22:09:37 +0800
categories: Rust Docker
---
在Docker环境中运行Rust应用是很容易的。alpine提供了极简版本的基础镜像。

# 1. docker开发环境准备

略。

# 2. 创建专用于编译的Docker镜像

[Dockerfile][rust-musl-builder-docker-file]，如果希望使用非CentOS系统进行编译，需要对脚本做一些修改。

# 3. 为编译定义别名

```bash
alias rbuild='docker run --rm -it -v "$(pwd)":/home/rust/src jiangz/rust:musl cargo build --release'
```

# 4. 编写Rust程序并编译

我们使用[Iron][github-iron]库，编写一个最简单的Web服务。

```rust
extern crate iron;

use iron::prelude::*;

fn hello_world(_: &mut Request) -> IronResult<Response> {
    Ok(Response::with((iron::status::Ok, "Hello World")))
}

fn main() {
    let chain = Chain::new(hello_world);
    Iron::new(chain).http("localhost:3000").unwrap();
}
```

# 5. 生成镜像并运行

```bash
cd target/
docker build . -t sampleweb
docker run --rm sampleweb
```

[rust-musl-builder-docker-file]: https://github.com/jiangzhe/rust-musl-builder/blob/master/Dockerfile
[github-iron]: https://github.com/iron/iron
