package cn.iamding.dfuse;

import cn.iamding.dfuse.config.DFuseProperties;
import cn.iamding.dfuse.config.FallbackListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Method implement of IDFuse
 * Inner package access, not public access
 */
class DFuseImpl implements IDFuse {

    private DFuseProperties properties;

    // 是否处于熔断状态
    private final AtomicBoolean degrade = new AtomicBoolean(false);

    private final AtomicInteger failCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);

    private static final ScheduledExecutorService SCHEDULE_POOL = Executors.newScheduledThreadPool(8);


    public DFuseImpl(DFuseProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getFallbackMethodName() {
        return properties.getFallbackMethod();
    }

    @Override
    public boolean isDegrade() {
        return degrade.get();
    }

    @Override
    public void setFailed(Throwable throwable) {
        failCount.incrementAndGet();
    }

    @Override
    public void complete() {
        totalCount.incrementAndGet();
        float currentFailRate = ((float) failCount.get() / (float) totalCount.get());
        if (totalCount.get() > properties.getRequestThreshold() && currentFailRate > properties.getDegradeFailRate()) {
            degrade.compareAndSet(false, true);
            SCHEDULE_POOL.schedule(() -> {
                totalCount.set(0);
                failCount.set(0);
                degrade.compareAndSet(true, false);
            }, properties.getRecoverTimeSeconds(), TimeUnit.SECONDS);
        }
    }

    @Override
    public FallbackListener getListener() {
        return properties.getFallbackListener();
    }
}
