package cn.iamding.dfuse.annotation;


import cn.iamding.dfuse.DFuseBuilder;
import cn.iamding.dfuse.IDFuse;
import cn.iamding.dfuse.config.DFuseProperties;
import cn.iamding.dfuse.exception.NotFoundMethodException;
import com.google.common.collect.Maps;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Aspect of DFuseDegrade
 */
@Aspect
public class DFuseDegradeAspect {

    // 保存不同方法的熔断器实例
    private final Map<String, IDFuse> fuseMap = Maps.newConcurrentMap();

    /**
     * 切面方法，注意这里execution是为了避开AJC重复调用切面方法的bug，详见https://bugs.eclipse.org/bugs/show_bug.cgi?id=274854
     */
    @Around("execution(* *(..)) &&@annotation(DFuseDegrade)")
    public Object degrade(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String fuseKey = getClass().getSimpleName() + "." + methodSignature.getName();
        IDFuse fuse = fuseMap.get(fuseKey);
        if (fuse == null) { //初始化方法的熔断器实例
            DFuseDegrade methodAnnotation = methodSignature.getMethod().getAnnotation(DFuseDegrade.class);
            int recoverTimeSeconds = methodAnnotation.recoverTimeSeconds();
            float degradeFailRate = methodAnnotation.degradeFailRate();
            int requestThreshold = methodAnnotation.requestThreshold();
            String fallbackMethodName = methodAnnotation.fallBackMethod();
            DFuseProperties properties = new DFuseProperties(fallbackMethodName, recoverTimeSeconds, degradeFailRate, requestThreshold);
            fuse = DFuseBuilder.build(properties);
            fuseMap.put(fuseKey, fuse);
        }
        Object result = null;
        try {
            if (!fuse.isDegrade()) {
                result = joinPoint.proceed(joinPoint.getArgs());
            } else {
                Method fallbackMethod = findMethod(methodSignature.getMethod().getDeclaringClass(), fuse.getFallbackMethodName(),
                        methodSignature.getMethod().getParameterTypes());
                if (fallbackMethod != null) {
                    result = fallbackMethod.invoke(joinPoint.getTarget(), joinPoint.getArgs());
                } else {
                    throw new NotFoundMethodException("Not Found Method:" + fuse.getFallbackMethodName());
                }
            }
        } catch (Throwable e) {
            fuse.setFailed(e);
            throw e;
        } finally {
            fuse.complete();
        }
        return result;
    }


    /**
     * 根据方法名找出同一个类中的方法对象
     */
    private static Method findMethod(Class<?> target, String methodName, Class<?>... pTypes) {
        if (methodName == null || "".equals(methodName)) {
            return null;
        }
        for (Method method : target.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (pTypes.length == 0 && parameterTypes.length == 0) {
                    method.setAccessible(true);
                    return method;
                }

                Class<?>[] origParamTypes = parameterTypes;
                int gap = parameterTypes.length - pTypes.length;
                if (gap == 1 || gap == 0) {
                    if (gap == 1) {
                        Class<?> lastParameter = parameterTypes[parameterTypes.length - 1];
                        // 最后一个必须是异常类型
                        if (!Throwable.class.isAssignableFrom(lastParameter)) {
                            continue;
                        }
                        origParamTypes = removeLastParameter(parameterTypes);
                    }
                    boolean match = true;
                    int index = 0;
                    for (Class<?> pType : origParamTypes) {
                        Class<?> expected = pTypes[index++];
                        if (pType != expected) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
        }
        return null;


    }

    /**
     * 删除最后一个元素
     */
    private static Class<?>[] removeLastParameter(Class<?>[] parameterTypes) {
        if (parameterTypes.length > 0) {
            Class<?>[] origParamTypes = new Class[parameterTypes.length - 1];
            System.arraycopy(parameterTypes, 0, origParamTypes, 0, parameterTypes.length - 1);
            return origParamTypes;
        }
        return null;
    }


}
