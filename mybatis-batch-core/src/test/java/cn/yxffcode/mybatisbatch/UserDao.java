package cn.yxffcode.mybatisbatch;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author gaohang on 16/7/29.
 */
public interface UserDao {

  @Insert({
          "insert into user (id, name) values (#{id}, #{name})"
  }) int batchInsert(List<User> users);

  @Select({
          "select id, name from user"
  })
  @Results({
          @Result(property = "id", column = "id"),
          @Result(property = "name", column = "name")
  })
  List<User> selectAll();
}
