package com.rpc.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.rpc.common.util.ConstantsUtils.DEFAULT_GROUP;
import static com.rpc.common.util.ConstantsUtils.DEFAULT_VERSION;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProvider {

    /**
     * 服务名
     */
    String value() default "";

    /**
     * 服务组别
     */
    String group() default DEFAULT_GROUP;

    /**
     * 服务版本号
     */
    String version() default DEFAULT_VERSION;
}
