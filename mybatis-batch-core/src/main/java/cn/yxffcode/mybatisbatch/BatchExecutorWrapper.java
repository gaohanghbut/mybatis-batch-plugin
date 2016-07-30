package cn.yxffcode.mybatisbatch;

import com.google.common.base.Throwables;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 7/30/16.
 */
public class BatchExecutorWrapper extends BatchExecutor {
  public BatchExecutorWrapper(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {

    final Map<String, Object> paramMap = (Map<String, Object>) parameter;
    final Object params = paramMap.get("param1");

    final Iterable<?> paramIterable = toIterable(params);
    try {
      for (Object obj : paramIterable) {
        super.update(ms, obj);
      }
      List<BatchResult> batchResults = doFlushStatements(false);
      if (batchResults == null || batchResults.size() == 0) {
        return 0;
      }
      return resolveUpdateResult(batchResults);
    } catch (Exception e) {
      doFlushStatements(true);
      Throwables.propagate(e);
      return 0;
    }
  }


  private Iterable<?> toIterable(final Object params) {
    if (params == null) {
      return Collections.emptyList();
    }
    Iterable<?> paramIterable;
    if (params instanceof Iterable) {
      paramIterable = (Iterable<?>) params;
    } else if (params.getClass().isArray()) {
      Object[] array = (Object[]) params;
      paramIterable = Arrays.asList(array);
    } else {
      paramIterable = Collections.singletonList(params);
    }
    return paramIterable;
  }

  private int resolveUpdateResult(final List<BatchResult> batchResults) {
    int result = 0;
    for (BatchResult batchResult : batchResults) {
      int[] updateCounts = batchResult.getUpdateCounts();
      if (updateCounts == null || updateCounts.length == 0) {
        continue;
      }
      for (int updateCount : updateCounts) {
        result += updateCount;
      }
    }
    return result;
  }

}