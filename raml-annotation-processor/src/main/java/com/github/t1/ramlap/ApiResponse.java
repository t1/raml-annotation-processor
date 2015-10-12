package com.github.t1.ramlap;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.ws.rs.core.Response.Status.*;

import java.lang.annotation.*;

import javax.ws.rs.core.Response.Status;

/**
 * Describes an interesting response of a HTTP method.
 * <p>
 * Similar to the same annotation from Swagger, but it's {@link Repeatable} and allows to use the {@link Status} enum.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Repeatable(ApiResponses.class)
public @interface ApiResponse {
    /**
     * Note: normally you'll get an error, when you specify a {@link #status()} <em>and</em> a {@link #statusCode()},
     * but this doesn't work for the default status.
     */
    public static final Status DEFAULT_STATUS = OK;

    /** If the code to be returned is not in the {@link Status JAX-RS enum}, use {@link #statusCode()} */
    Status status() default OK;

    /** Numeric status code. You should prefer to use the more expressive {@link #status()} instead. */
    int statusCode() default -1;

    String message() default "";

    Class<?> type() default Void.class;

    ApiResponseHeader[] responseHeaders() default {};
}
