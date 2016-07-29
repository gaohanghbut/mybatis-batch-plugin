package cn.yxffcode.mybatisbatch;

import com.google.common.base.MoreObjects;

/**
 * @author gaohang on 16/7/29.
 */
public class User {
  private int id;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("name", name)
            .toString();
  }
}
