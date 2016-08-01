# mybatis-batch-plugin 
此插件基于BatchExecutor实现批量更新，只需要将需要更新的sql id(不包含命名空间)以batch开头，参数需要是Iterable或者数组即可。

## 实现目的 
mybatis已有的批量更新比较麻烦，要么写动态sql，要么利用BatchExecutor的SqlSession.
在工程中,更加希望DAO中的方法需要批量的时候用批量,不需要批量的时候不用批量.
有两种方式实现,一种是实现自定义的Executor,它持有batch与非batch的两个Executor,在执行sql时自由切换,第二种实现方式则是通过mybatis
插件实现,当需要使用批量时,不使用sqlsession中的executor,而是使用新的executor.
第一种方式相对稍复杂一点,第二种方式需要将此插件配置成第一个Executor插件.

## mybatis插件的使用方式 
### mybatis配置 
```xml
<plugin interceptor="cn.yxffcode.mybatisbatch.BatchExecutorInterceptor"></plugin>
```
DAO如果需要使用batch则,参数需要是Iterable或者数组,sql的statement id(不包含命名空间)要以batch开头,如果是映射接口,则方法名以batch开头 
```java
public interface UserDao {

  @Insert({
          "insert into user (id, name) values (#{id}, #{name})"
  }) int batchInsert(List<User> users);
}
```

### 插件的不足
此插件的实现原理是拦截Executor的update方法,然后将目标方法的调用改为创建新的BatchExecutor,然后执行批量的更新,
但新的BatchExecutor对象没有经过InterceptorChain的包装,所以在此插件之前的Executor拦截器不会被执行,所以最好是将此插件配置在第一个.

## 使用新的SqlSessionFactory
### Spring配置
替换SqlSessionFactoryBean中的SqlSessionFactoryBuilder为FlexSqlSessionFactoryBuilder 
```xml 
<bean class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:mybatis-config.xml"/>
    <property name="sqlSessionFactoryBuilder">
        <bean class="cn.yxffcode.mybatisbatch.FlexSqlSessionFactoryBuilder"/>
    </property>
</bean>
```
FlexSqlSessionFactoryBuilder会创建FlexSqlSessionFactory,它使用FlexExecutor对原始的Executor做包装

### 使用方式
除了配置之外,DAO代码的使用方式与插件相同,参数需要是Iterable或者数组,sql的statement id(不包含命名空间)要以batch开头如果是映射接口,则方法名以batch开头
