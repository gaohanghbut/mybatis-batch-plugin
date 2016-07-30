# mybatis-batch-plugin
mybatis已有的批量更新比较麻烦，要么写动态sql，要么利用BatchExecutor的SqlSession, 
此插件基于BatchExecutor实现批量更新，只需要将需要更新的sql id以batch开头，参数需要是Iterable或者数组即可。

## 使用方式 
mybatis配置 
```xml
<plugin interceptor="cn.yxffcode.mybatisbatch.BatchExecutorInterceptor"></plugin>
```
DAO如果需要使用batch,则sql的statement id要以batch开头,如果是映射接口,则方法名以batch开头,参数需要是Iterable或者数组 
```java
public interface UserDao {

  @Insert({
          "insert into user (id, name) values (#{id}, #{name})"
  }) int batchInsert(List<User> users);
}
```