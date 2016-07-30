# mybatis-batch-plugin
mybatis已有的批量更新比较麻烦，要么写动态sql，要么利用BatchExecutor的SqlSession, 
此插件基于BatchExecutor实现批量更新，只需要将需要更新的sql id(不包含命名空间)以batch开头，参数需要是Iterable或者数组即可。

## 使用方式 
mybatis配置 
```xml
<plugin interceptor="cn.yxffcode.mybatisbatch.BatchExecutorInterceptor"></plugin>
```
DAO如果需要使用batch,则sql的statement id(不包含命名空间)要以batch开头,如果是映射接口,则方法名以batch开头,参数需要是Iterable或者数组 
```java
public interface UserDao {

  @Insert({
          "insert into user (id, name) values (#{id}, #{name})"
  }) int batchInsert(List<User> users);
}
```

## 不足
此插件的实现原理是拦截Executor的update方法,然后将目标方法的调用改为创建新的BatchExecutor,然后执行批量的更新,
但新的BatchExecutor对象没有经过InterceptorChain的包装,所以在此插件之前的Executor拦截器不会被执行,所以最好是将此插件配置在第一个,