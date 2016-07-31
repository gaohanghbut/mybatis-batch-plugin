package cn.yxffcode.mybatisbatch.collection;

import java.util.AbstractList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 表示多个{@link List}的一个视图，不能调用{@link GroupList#add(Object)}和{@link GroupList#remove(int)}方法
 *
 * @author gaohang on 15/8/21.
 */
public class GroupList<E> extends AbstractList<E> {

  private List<? extends E>[] lists;
  private int size;

  private GroupList(List<? extends E>... lists) {
    this.lists = lists;
    for (List<? extends E> list : lists) {
      size += list.size();
    }
  }

  public static <T> GroupList<T> create(List<? extends T>... lists) {
    return new GroupList<>(lists);
  }

  @Override public E get(int index) {
    checkArgument(index >= 0 && index < size);
    //如果子集合比较多，可以换成二分查找
    int remaining = index;
    for (List<? extends E> list : lists) {
      if (list.size() > remaining) {
        return list.get(remaining);
      }
      remaining -= list.size();
    }
    return null;
  }

  @Override public int size() {
    return size;
  }
}
