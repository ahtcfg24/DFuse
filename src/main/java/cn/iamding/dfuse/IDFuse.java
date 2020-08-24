package cn.iamding.dfuse;

import cn.iamding.dfuse.config.FallbackListener;

/**
 * IDFuse method definition
 */
public interface IDFuse {

    String getFallbackMethodName();

    boolean isDegrade();

    void setFailed(Throwable throwable);

    void complete();

    FallbackListener getListener();

}

