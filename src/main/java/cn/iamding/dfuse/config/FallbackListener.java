package cn.iamding.dfuse.config;

/**
 * Fallback method runner
 */
public interface FallbackListener<T> {

    T run(Object[] params);
}
