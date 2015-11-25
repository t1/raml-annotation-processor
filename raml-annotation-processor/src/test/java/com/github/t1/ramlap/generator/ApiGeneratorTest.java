package com.github.t1.ramlap.generator;

import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static javax.tools.StandardLocation.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.assertj.core.api.Assert;
import org.junit.Test;

import com.github.t1.exap.reflection.Package;

public class ApiGeneratorTest {
    private static final Package PACKAGE = ENV.type(ApiGeneratorTest.class).getPackage();

    private Assert<?, String> assertThatGeneratedClass(Package pkg, String className) {
        return assertThat(ENV.getCreatedResource(SOURCE_OUTPUT, getClass().getPackage().getName(), className))
                .describedAs("created class resource %s ### %s.\nactually found: %s",
                        pkg, className, ENV.getCreatedResources());
    }

    @Test
    public void shouldGenerateTestApi() {
        new ApiGenerator(PACKAGE).generate();

        assertThatGeneratedClass(PACKAGE, "TestApi")
                .isEqualTo(contentOf(new File(
                        "src/test/java/" + getClass().getPackage().getName().replace('.', '/') + "/TestApi.java")));
    }
}
