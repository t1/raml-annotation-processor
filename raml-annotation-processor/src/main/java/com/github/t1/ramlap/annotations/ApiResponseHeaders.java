package com.github.t1.ramlap.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Target(METHOD)
@Retention(RUNTIME)
public @interface ApiResponseHeaders {
    ApiResponseHeader[] value();
}
