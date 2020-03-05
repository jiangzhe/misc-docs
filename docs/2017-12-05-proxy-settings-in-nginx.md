# Nginx的Proxy设置

最近在工作中用到nginx做反向代理，遇到一个与跳转相关的问题，涉及到了nginx里的几个配置项，在这里疏理一下。

## 配置项

- proxy_pass

配置请求的代理地址。
需要注意的是结尾“/”字符。

| location  | proxy_pass  | request_uri | proxied_url |
| :--- | :--- | :--- | :--- |
| /dir1  | http://upstream1 | /dir1/a/b/c | http://upstream1/dir1/a/b/c |
|   -    |        -         | /dir1xyz    | http://upstream1/dir1xyz |
| /dir2  | http://upstream2/ | /dir2/a/b/c | http://upstream2//a/b/c |
|   -    |        -          | /dir2xyz    | http://upstream2/xyz |
| /dir3  | http://upstream3/sub3 | /dir3/a/b/c | http://upstream3/sub3/a/b/c |
|   -    |        -              | /dir3xyz    | http://upstream3/sub3xyz |
| /dir4  | http://upstream4/sub4/ | /dir4/a/b/c | http://upstream4/sub4//a/b/c |
|   -    |        -               | /dir4xyz    | http://upstream4/sub4/xyz |
| /dir5/ | http://upstream5  | /dir5/a/b/c | http://upstream5/dir5/a/b/c |
| /dir6/ | http://upstream6/ | /dir6/a/b/c | http://upstream6/a/b/c |
| /dir7/ | http://upstream7/sub7  | /dir7/a/b/c | http://upstream7/sub7a/b/c |
| /dir8/ | http://upstream8/sub8/ | /dir8/a/b/c | http://upstream8/sub8/a/b/c |

- proxy_set_header和proxy_redirect

主要想讲一下Host这个Http头部以及30x重定向。
假设构建一个简单的Spring Boot应用：

```java
@Controller
@SpringBootApplication
@EnableSpringHttpSession
public class RedirectTestApplication {
    @RequestMapping("/redirect")
    public String redirect() {
        return "redirect:/";
    }
    ...
}
```

用以下命令测试：

```bash
curl -kvL -H 'Host: www.baidu.com' http://localhost:8080/redirect/
```

啊喔，重定向到了百度？Web服务器是通过请求的Host头部与路径组合来生成重定向地址的。

Nginx默认设置 proxy_set_header Host $proxy_host，即将upstream名称设置成Host头部值。

同时，Nginx默认设置proxy_redirect default，会检测30x报文，尝试匹配upstream名称并修改为自身服务和路由的地址。

假设我们设置了规则：

```nginx
upstream upstream9 { server 127.0.0.1:8080; }
location /up9/ { proxy_pass http://upstream9/; }
```

则Nginx会在请求报文添加头部Host: upstream9。
当服务端返回重定向，头部为Location: http://upstream9/some/path/redirected/。
Nginx匹配并修改为 Location: http://nginx_server:nginx_port/up9/some/path/redirected/。

如果设置：
```nginx
proxy_redirect off;
```

我们就会发现，Location保持原值不变：http://upstream9/some/path/redirected/。

一般情况下，不需要在Nginx中设置Host头部的。
