package com.github.t1.ramlap;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Target(METHOD)
@Retention(RUNTIME)
public @interface ApiResponses {
    ApiResponse[] value();
}