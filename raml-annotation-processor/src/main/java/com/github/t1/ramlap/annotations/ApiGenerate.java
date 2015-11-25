package com.github.t1.ramlap.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Target({ PACKAGE })
@Retention(RUNTIME)
@Inherited
public @interface ApiGenerate {
    String[] from();
}
