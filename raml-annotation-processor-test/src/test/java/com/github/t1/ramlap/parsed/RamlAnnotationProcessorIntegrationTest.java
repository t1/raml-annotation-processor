package com.github.t1.ramlap.parsed;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.junit.Test;

public class RamlAnnotationProcessorIntegrationTest {
    private static final File EXPECTED_FILE = new File("src/test/resources/expected.raml");
    private static final File ACTUAL_FILE = new File("target/classes/doc/test-title.raml");

    @Test
    public void shouldHaveProducedRaml() {
        assertThat(ACTUAL_FILE).hasSameContentAs(EXPECTED_FILE);
    }
}
