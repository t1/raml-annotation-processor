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
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Inherited
@Repeatable(ApiResponses.class)
public @interface ApiResponse {
    /**
     * Note: normally you'll get an error, when you specify a {@link #status()} <em>and</em> a {@link #statusCode()},
     * but this doesn't work for the default status. {@link Status#BAD_REQUEST} is the most commonly used custom error.
     */
    public static final Status DEFAULT_STATUS = BAD_REQUEST;

    /** If the code to be returned is not in the {@link Status JAX-RS enum}, use {@link #statusCode()} */
    Status status() default BAD_REQUEST;

    /** Numeric status code. You should prefer to use the more expressive {@link #status()} instead. */
    int statusCode() default -1;

    /**
     * A short, human-readable summary of the problem type. It SHOULD NOT change from occurrence to occurrence of the
     * problem, except for purposes of localization.
     * 
     * @see ProblemDetail#title(String)
     */
    String title() default "";

    /**
     * The full, human-readable explanation specific to this occurrence of the problem. It MAY change from occurrence to
     * occurrence of the problem.
     * 
     * @see ProblemDetail#detail(String)
     */
    String detail() default "";

    /**
     * The type of the response, i.e. the body to be returned. If that class itself is annotated as {@link ApiResponse},
     * the {@link #status()} of that annotation inherited. If the type is not specified, the return type of the method
     * is used.
     */
    Class<?> type() default Void.class;

    /**
     * Additional headers that are returned by this response.
     */
    ApiResponseHeader[] responseHeaders() default {};
}
