package cn.iamding.dfuse.config;

/**
 * DFuse CircuitBreaker Config
 */
public class DFuseProperties {

    private int recoverTimeSeconds;
    private float degradeFailRate;
    private int requestThreshold;
    private String fallbackMethod;
    private FallbackListener fallbackListener;


    public DFuseProperties(String fallbackMethod, int recoverTimeSeconds, float degradeFailRate, int requestThreshold) {
        this.fallbackMethod = fallbackMethod;
        this.recoverTimeSeconds = recoverTimeSeconds;
        this.degradeFailRate = degradeFailRate;
        this.requestThreshold = requestThreshold;
    }

    public DFuseProperties(FallbackListener fallbackListener, int recoverTimeSeconds, float degradeFailRate, int requestThreshold) {
        this.fallbackListener = fallbackListener;
        this.recoverTimeSeconds = recoverTimeSeconds;
        this.degradeFailRate = degradeFailRate;
        this.requestThreshold = requestThreshold;
    }

    public FallbackListener getFallbackListener() {
        return fallbackListener;
    }

    public void setFallbackListener(FallbackListener fallbackListener) {
        this.fallbackListener = fallbackListener;
    }

    public int getRecoverTimeSeconds() {
        return recoverTimeSeconds;
    }

    public float getDegradeFailRate() {
        return degradeFailRate;
    }

    public int getRequestThreshold() {
        return requestThreshold;
    }

    public String getFallbackMethod() {
        return fallbackMethod;
    }

    public void setRecoverTimeSeconds(int recoverTimeSeconds) {
        this.recoverTimeSeconds = recoverTimeSeconds;
    }

    public void setDegradeFailRate(float degradeFailRate) {
        this.degradeFailRate = degradeFailRate;
    }

    public void setRequestThreshold(int requestThreshold) {
        this.requestThreshold = requestThreshold;
    }

    public void setFallbackMethod(String fallbackMethod) {
        this.fallbackMethod = fallbackMethod;
    }

}
