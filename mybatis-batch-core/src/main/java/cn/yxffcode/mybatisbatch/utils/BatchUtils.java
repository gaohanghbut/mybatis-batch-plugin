package cn.yxffcode.mybatisbatch.utils;

/**
 * @author gaohang on 16/7/31.
 */
public abstract class BatchUtils {
    private BatchUtils() {
    }

    public static boolean shouldDoBatch(final String statementId) {
        return statementId.startsWith("batch", statementId.lastIndexOf('.') + 1);
    }
}
