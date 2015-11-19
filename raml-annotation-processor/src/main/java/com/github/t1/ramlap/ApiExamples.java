package com.github.t1.ramlap;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface ApiExamples {
    ApiExample[] value();
}
