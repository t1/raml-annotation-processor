package com.github.t1.ramlap;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Target(METHOD)
@Retention(RUNTIME)
@Repeatable(ApiResponseHeaders.class)
public @interface ApiResponseHeader {
    String name();

    String description() default "";

    Class<?> type() default Void.class;
}
