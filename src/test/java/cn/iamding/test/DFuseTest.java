package cn.iamding.test;

import cn.iamding.dfuse.DFuseBuilder;
import cn.iamding.dfuse.IDFuse;
import cn.iamding.dfuse.annotation.DFuseDegrade;
import cn.iamding.dfuse.config.DFuseProperties;
import cn.iamding.dfuse.config.FallbackListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UnitTest of DFuse
 */
public class DFuseTest {

    private final Random random = new Random();

    //用于模拟请求的线程池
    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    //api方式接入需要初始化fuse对象
    private IDFuse fuse;

    /**
     * 测试API方式的熔断
     */
//    @Test
    public void testByApi() throws Exception {
        FallbackListener<String> fallbackListener = params -> {
            String param = (String) params[0];
            return fallback(param);
        };
        fuse = DFuseBuilder.build(new DFuseProperties(fallbackListener, 5, 0.3f, 15));
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            final int j = i;
            pool.execute(() -> {
                try {
                    System.out.println("请求" + j + "结果:" + degradeByApi("testApi"));
                } catch (Exception e) {
                    System.out.println("请求" + j + "结果:发生异常" + e.getClass());
                }finally {
                    latch.countDown();
                }

            });

        }
        latch.await();
    }

    /**
     * 需要熔断的方法，API形式接入
     */
    private String degradeByApi(String param) {
        try {
            if (!fuse.isDegrade()) {
                int ran = random.nextInt(10);
                if (ran % 2 == 0) { //模拟偶数情况失败
                    throw new NumberFormatException();
                }
                return "success:" + param;
            } else {
                return (String) fuse.getListener().run(new Object[]{ param });
            }
        } catch (Exception e) {
            fuse.setFailed(e);
            throw e;
        } finally {
            fuse.complete();
        }


    }

    /**
     * 测试注解形式的熔断
     */
//    @Test
    public void testByAnnotation() throws Exception {

        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            final int j = i;
            pool.execute(() -> {
                try {
                    System.out.println("请求" + j + "结果:" + degradeByAnnotation("testAnnotation"));
                } catch (Exception e) {
                    System.out.println("请求" + j + "结果:发生异常" + e.getClass());
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    /**
     * 需要熔断的方法，注解形式接入，注解的fallBackMethod必须能在同一个类中找到
     */
    @DFuseDegrade(fallBackMethod = "fallback", degradeFailRate = 0.2f, requestThreshold = 10, recoverTimeSeconds = 5)
    public String degradeByAnnotation(String param) {
        int ran = random.nextInt(100);
        if (ran % 2 == 0) {
            throw new NumberFormatException();
        }
        return "success:" + param;

    }

    /**
     * 降级逻辑
     */
    public String fallback(String params) {
        return "fallback:" + params;
    }

}
