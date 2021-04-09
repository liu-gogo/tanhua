package com.itheima.tanhua.server.utils;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE,ElementType.METHOD})
@Documented
public @interface Cache {
    long time() default 60;
}
