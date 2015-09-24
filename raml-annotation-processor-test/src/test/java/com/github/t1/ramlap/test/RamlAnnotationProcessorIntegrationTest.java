package com.github.t1.ramlap.test;

import static org.assertj.core.api.StrictAssertions.*;

import java.io.File;

import org.junit.*;

public class RamlAnnotationProcessorIntegrationTest {
    private static final File EXPECTED_FILE = new File("src/test/resources/expected.raml");
    private static final File ACTUAL_FILE = new File("target/classes/doc/api.raml");

    @Test
    @Ignore
    public void shouldHaveProducedRaml() {
        assertThat(ACTUAL_FILE).hasSameContentAs(EXPECTED_FILE);
    }
}
