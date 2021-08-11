package com.linyous.mqtt.spi.annotation;

import java.lang.annotation.*;

/**
 * @author Linyous
 * @date 2021/6/21 10:19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MiniSPI {
    /**
     * 默认扩展器名
     */
    String value() default "";
}
