
[![Maven Central](https://img.shields.io/maven-central/v/org.noear/nami-springboot-starter.svg)](https://search.maven.org/artifact/org.noear/nami-springboot-starter)
[![Apache 2.0](https://img.shields.io/:license-Apache2-blue.svg)](https://license.coscl.org.cn/Apache2/)
[![JDK-8+](https://img.shields.io/badge/JDK-8+-green.svg)](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
[![QQ交流群](https://img.shields.io/badge/QQ交流群-22200020-orange)](https://jq.qq.com/?_wv=1027&k=kjB5JNiC)




# nami-springboot-starter



## 1、nami 基础使用参考：

[https://gitee.com/noear/solon/tree/master/_nami](https://gitee.com/noear/solon/tree/master/_nami)


## 2、nami-springboot-starter 使用参考：

服务配置

```yaml
spring:
  nami:
    #配置服务地址，可选。未配置时直接在@NamiClient中配置url即可
    services:
      baidu: https://www.baidu.com,http://220.181.38.150
    #可选配置,多个包用","分隔
    packages: org.noear.nami.demo

```

接口申明

```java
@NamiClient(name = "baidu")
public interface BaiduApi {
    @Mapping("GET s")
    String search(String w);
    
    @Mapping("GET s")
    Result test2(String w);
    
    default void doTest(){
        for(int i=0;i<10;i++){
          Result r=  this.test2("测试");
            System.out.println(r.code());
        }
    }
}
```

接口应用与测试

```java
@Service
public class TestService {
    @Autowired
    BaiduApi api;
    
    @PostConstruct
    public void init(){
        System.out.println("测试");
        System.out.println(api.search("测试1"));
        api.doTest();

    }
}
```

