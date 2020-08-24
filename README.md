# Java简易熔断器——DFuse
---
## 一、项目简介
一个简易的本地熔断器，可以对项目中的某个逻辑增加熔断降级功能，当某块逻辑代码出现异常率达到指定百分比时就会触发降级，后续该块逻辑代码就不再执行，转而执行预先定义的降级逻辑代码，直到指定的恢复时间后自动恢复。
基于 Java 1.8 实现，支持API方式或注解方式使用。
## 二、使用方式
#### 2.1 通过maven引入jar包
当前jar包尚未上传到maven中央仓库，有需要可以自行clone到本地install到本地，再以jar包的形式依赖到项目中
* 本地jar包依赖方式
```xml
 <dependency>
        <groupId>cn.iamding</groupId>   
        <artifactId>dfuse</artifactId>
        <version>${dfuse-version}</version>
        <scope>system</scope>
        <systemPath>${jar存放目录路径}\dfuse-${dfuse-version}.jar</systemPath>
 </dependency>
```
* maven中央仓库依赖方式
```xml
 <dependency>
        <groupId>cn.iamding</groupId>   
        <artifactId>dfuse</artifactId>
        <version>${dfuse-version}</version>
 </dependency>
```                
${dfuse-version}版本变更记录

版本号|变更时间|变更内容                      
:-:  | :-:  | :-:                                     
1.0.0-SNAPSHOT|20200824|增加api方式和注解方式熔断降级功能 

#### 2.2 对熔断代码块增加降级逻辑
对于一段要熔断的代码块或方法，有API和注解两种方式添加熔断，要熔断的方法示例如下
```java
public String getMsg(String param) {
    return mysqlClient.getMsgConntentByParam(param);
}
```
降级的逻辑示例如下，fallback方法的参数和返回值可以自定义，但参数和返回值要和被熔断方法相同
```java
public String fallback(String param) {
        return "fallback:" + params;
}
```
有两种熔断方式触发降级，任意一种即可
* API方式熔断

1 . 创建降级回调Listener，在Listener的回调中调用降级逻辑fallback
```java
FallbackListener<String> fallbackListener = params -> {
            String param = (String) params[0];
            return fallback(param);
        };
``` 
2 . 初始化fuse，每个方法只需要初始化一个fuse实例
```java
 IDFuse fuse = DFuseBuilder.build(new DFuseProperties(fallbackListener, 10, 0.3f, 15));
```
3 . 使用fuse保护代码块，在降级时调用降级Listener的run方法，并传入参数列表
```java
public String getMsg(String param) {
        try {
            if (!fuse.isDegrade()) {
                return mysqlClient.getMsgConntentByParam(param);
            } else {
                return (String) fuse.getListener().run(new Object[]{ param });
            }
        } catch (Exception e) {
            fuse.setFailed(e);
            throw e;
        } finally {
            fuse.complete();
        }
}
```

* 注解方式熔断（推荐）

1 . 在要注解的方法上增加@DFuseDegrade注解
```java
@DFuseDegrade(fallBackMethod = "fallback", degradeFailRate = 0.2f, requestThreshold = 30, recoverTimeSeconds = 10)
public String getMsg(String param) {
    return mysqlClient.getMsgConntentByParam(param);
}
```
2 . 在fallBackMethod属性中制定降级时调用的方法名，必须和熔断保护的代码块参数、返回值均相同

#### 2.3 fuse及DFuseDegrade属性含义说明

属性|含义|示例值|说明                      
:-:  | :-:  | :-:  |:-:                                     
fallBackMethod|要调用的降级逻辑方法名|fallback|仅注解方式需要
degradeFailRate|失败率高于degradeFailRate触发降级|0.2f|20%的失败率就会触发降级
requestThreshold|请求数超过requestThreshold才会判断degradeFailRate是否达到阈值|30|请求数超过该阈值才会统计失败率
recoverTimeSeconds|多长时间后自动恢复（单位秒）|20|20秒后无论服务是否恢复，降级逻辑都被关闭
