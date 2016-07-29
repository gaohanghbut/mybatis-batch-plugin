package cn.yxffcode.mybatisbatch;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author gaohang on 16/7/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring.xml")
public class BatchExecutorInterceptorTest {

  @Resource
  private SqlSessionFactory sqlSessionFactory;

  @Test
  public void testBatch() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserDao userDao = sqlSession.getMapper(UserDao.class);

    List<User> users = Lists.newArrayList();
    for (int i = 0; i < 100; i++) {
      User user = new User();
      user.setId(i);
      user.setName("name:" + i);
      users.add(user);
    }

    try {
      userDao.batchInsert(users);
      List<User> results = userDao.selectAll();
      for (User result : results) {
        System.out.println(result);
      }
    } catch (Exception e) {
      sqlSession.rollback();
      Throwables.propagate(e);
    } finally {
      sqlSession.close();
    }
  }
}
