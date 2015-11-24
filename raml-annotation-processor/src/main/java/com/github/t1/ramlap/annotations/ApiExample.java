package com.github.t1.ramlap.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import io.swagger.annotations.ApiModelProperty;

/**
 * The OPTIONAL example property may be used to attach one or more examples of a type's instance to the type
 * declaration. Its value is an instance of the type. It is highly RECOMMENDED that API documentation include a rich
 * selection of examples.
 * <p>
 * For simple examples, it may be sufficient to use the Swagger {@link ApiModelProperty#example()}.
 * <p>
 * <b>IMPORTANT</b>: For RAML 0.8 documents, only the {@link #value()} is considered.
 *
 * @since RAML 1.0
 */
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Inherited
@Repeatable(ApiExamples.class)
public @interface ApiExample {
    /** An alternate, human-friendly name for the example */
    String displayName() default "";

    /** A longer, human-friendly description of the example markdown string */
    String description() default "";

    // (<annotationName>)? Annotations to be applied to this example. Annotations are any property whose key begins with
    // "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared
    // annotation name. See the section on annotations. A value corresponding to the declared type of this annotation.

    /** The `content` example itself. Either a valid value for this type or the serialized version of a valid value */
    String value();

    /**
     * By default, examples are validated against any type declaration. Set this to false to allow examples that need
     * not validate.
     */
    boolean strict() default true;
}
