package cn.yxffcode.mybatisbatch.utils;

import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;

import java.lang.reflect.Proxy;

/**
 * @author gaohang on 16/8/1.
 */
public abstract class ExecutorUtils {
  private ExecutorUtils() {
  }

  public static Executor getTargetExecutor(final Executor executor) {
    Executor targetExecutor = executor;
    while (targetExecutor instanceof Proxy) {
      targetExecutor = (Executor) Reflections.getField("target",
              Proxy.getInvocationHandler(targetExecutor));
    }
    //取真正的executor
    if (targetExecutor instanceof CachingExecutor) {
      targetExecutor = (Executor) Reflections.getField("delegate", targetExecutor);
    }
    return targetExecutor;
  }
}
