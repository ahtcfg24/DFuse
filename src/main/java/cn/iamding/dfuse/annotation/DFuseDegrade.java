package cn.iamding.dfuse.annotation;

import cn.iamding.dfuse.constants.DFuseConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation of DFuse
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DFuseDegrade {

    String fallBackMethod();

    int recoverTimeSeconds() default DFuseConstants.DEFAULT_RECOVER_TIME_SECONDS;

    float degradeFailRate() default DFuseConstants.DEFAULT_ERROR_PERCENT;

    int requestThreshold() default DFuseConstants.DEFAULT_REQUEST_THRESHOLD;

}

