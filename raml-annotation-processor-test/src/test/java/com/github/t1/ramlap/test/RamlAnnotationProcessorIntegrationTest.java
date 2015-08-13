package com.github.t1.ramlap.test;

import static org.assertj.core.api.StrictAssertions.*;

import java.io.File;

import org.junit.Test;

public class RamlAnnotationProcessorIntegrationTest {
    private static final File EXPECTED_FILE = new File("src/test/resources/expected-raml.yaml");
    private static final File ACTUAL_FILE = new File("target/generated-sources/annotations/raml.yaml");

    @Test
    public void shouldHaveProducedRaml() {
        assertThat(ACTUAL_FILE).hasSameContentAs(EXPECTED_FILE);
    }
}
