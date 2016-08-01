package cn.yxffcode.mybatisbatch;

import cn.yxffcode.mybatisbatch.collection.GroupList;
import cn.yxffcode.mybatisbatch.utils.BatchUtils;
import cn.yxffcode.mybatisbatch.utils.ExecutorUtils;
import cn.yxffcode.mybatisbatch.utils.Reflections;
import com.google.common.base.Throwables;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 用于切换batch与非batch的Executor
 * <p/>
 * 批量只处理更新,与查询相关的全部由非批量的executor处理,包括query,cache等
 *
 * @author gaohang on 16/7/31.
 */
public class FlexExecutor implements Executor {

  private final Executor rawExecutor;
  private final Configuration configuration;
  private BatchExecutorAdaptor batchExecutor;

  public FlexExecutor(final Executor rawExecutor, final Configuration configuration) {
    this.rawExecutor = rawExecutor;
    this.configuration = configuration;
  }

  @Override public int update(final MappedStatement ms, final Object parameter) throws SQLException {
    if (!BatchUtils.shouldDoBatch(ms.getId()) || rawExecutor instanceof BatchExecutor) {
      return rawExecutor.update(ms, parameter);
    }
    if (batchExecutor == null) {
      final Executor rawExecutor = ExecutorUtils.getTargetExecutor(this.rawExecutor);
      batchExecutor = new BatchExecutorAdaptor(configuration, rawExecutor.getTransaction());
      InterceptorChain interceptorChain =
              (InterceptorChain) Reflections.getField("interceptorChain", configuration);
      batchExecutor = (BatchExecutorAdaptor) interceptorChain.pluginAll(batchExecutor);
    }
    return batchExecutor.update(ms, parameter);
  }

  @Override
  public <E> List<E> query(final MappedStatement ms, final Object parameter, final RowBounds rowBounds,
                           final ResultHandler resultHandler,
                           final CacheKey cacheKey, final BoundSql boundSql) throws SQLException {
    return rawExecutor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
  }

  @Override
  public <E> List<E> query(final MappedStatement ms, final Object parameter, final RowBounds rowBounds,
                           final ResultHandler resultHandler) throws SQLException {
    return rawExecutor.query(ms, parameter, rowBounds, resultHandler);
  }

  @Override public List<BatchResult> flushStatements() throws SQLException {
    List<BatchResult> batchResults = rawExecutor.flushStatements();
    if (batchExecutor == null) {
      return batchResults;
    }
    if (isEmpty(batchResults)) {//may always be true
      return batchExecutor.flushStatements();
    }
    return GroupList.create(batchExecutor.flushStatements(), batchResults);
  }

  @Override public void commit(final boolean required) throws SQLException {
    SQLException ex = null;
    if (batchExecutor != null) {
      try {
        batchExecutor.commit(required);
      } catch (SQLException e) {
        ex = e;
      }
    }
    try {
      rawExecutor.commit(required);
    } catch (SQLException e) {
      ex = e;
    }
    if (ex != null) {
      throw ex;
    }
  }

  @Override public void rollback(final boolean required) throws SQLException {
    SQLException ex = null;
    if (batchExecutor != null) {
      try {
        batchExecutor.rollback(required);
      } catch (SQLException e) {
        ex = e;
      }
    }
    try {
      rawExecutor.rollback(required);
    } catch (SQLException e) {
      ex = e;
    }
    if (ex != null) {
      throw ex;
    }
  }

  @Override
  public CacheKey createCacheKey(final MappedStatement ms, final Object parameterObject, final RowBounds rowBounds,
                                 final BoundSql boundSql) {
    return rawExecutor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
  }

  @Override public boolean isCached(final MappedStatement ms, final CacheKey key) {
    return rawExecutor.isCached(ms, key);
  }

  @Override public void clearLocalCache() {
    try {
      rawExecutor.clearLocalCache();
    } finally {
      if (batchExecutor != null) {
        batchExecutor.clearLocalCache();
      }
    }
  }

  @Override public void deferLoad(final MappedStatement ms, final MetaObject resultObject, final String property,
                                  final CacheKey key) {
    //只与查询相关
    rawExecutor.deferLoad(ms, resultObject, property, key);
  }

  @Override public Transaction getTransaction() {
    return rawExecutor.getTransaction();
  }

  @Override public void close(final boolean forceRollback) {

    Exception ex = null;
    if (batchExecutor != null) {
      try {
        batchExecutor.close(forceRollback);
      } catch (Exception e) {
        ex = e;
      }
    }
    rawExecutor.close(forceRollback);
    if (ex != null) {
      Throwables.propagate(ex);
    }
  }

  @Override public boolean isClosed() {
    return rawExecutor.isClosed();
  }

  private boolean isEmpty(final List<BatchResult> batchResults) {
    return batchResults == null || batchResults.size() == 0;
  }

}
