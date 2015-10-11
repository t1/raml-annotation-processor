package com.github.t1.ramlap;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import javax.ws.rs.core.Response.Status;

@Target(METHOD)
@Retention(RUNTIME)
@Repeatable(ApiResponses.class)
public @interface ApiResponse {

    Status status();

    String message();

    Class<?> type() default Void.class;

    ApiResponseHeader[] responseHeaders() default @ApiResponseHeader()
    ;
}
