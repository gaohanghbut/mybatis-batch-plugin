package cn.yxffcode.mybatisbatch;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * @author gaohang on 16/8/1.
 */
public class FlexSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Configuration config) {
    return new FlexSqlSessionFactory(config);
  }

}
