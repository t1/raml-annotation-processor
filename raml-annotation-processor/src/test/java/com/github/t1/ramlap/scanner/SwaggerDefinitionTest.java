package com.github.t1.ramlap.scanner;

import static java.util.Arrays.*;
import static org.raml.model.Protocol.*;

import javax.annotation.processing.Messager;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Raml;

import com.github.t1.ramlap.scanner.RamlScanner;

import io.swagger.annotations.*;
import io.swagger.annotations.SwaggerDefinition.Scheme;

@RunWith(MockitoJUnitRunner.class)
public class SwaggerDefinitionTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private Messager messager;

    private final RamlScanner scanner = new RamlScanner();

    private Raml scanSwaggerDefinition(Class<?> container) {
        scanner.scan(container.getAnnotation(SwaggerDefinition.class));
        return scanner.getResult();
    }

    @Test
    public void shouldScanBasicInfos() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v") )
        class Dummy {}

        Raml raml = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(raml.getTitle()).isEqualTo("ti");
        softly.assertThat(raml.getVersion()).isEqualTo("v");
        // TODO String mediaType;
    }

    @Test
    public void shouldStripEmptyVersion() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "") )
        class Dummy {}

        Raml raml = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(raml.getTitle()).isEqualTo("ti");
        softly.assertThat(raml.getVersion()).isNull();
    }

    @Test
    public void shouldScanTitleWithSpace() {
        @SwaggerDefinition(info = @Info(title = "ti tle", version = "") )
        class Dummy {}

        Raml raml = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(raml.getTitle()).isEqualTo("ti tle");
        softly.assertThat(raml.getVersion()).isNull();
    }

    @Test
    public void shouldScanBasePath() {
        @SwaggerDefinition(basePath = "http://{host}/{path}", schemes = Scheme.HTTPS)
        class Dummy {}

        Raml raml = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(raml.getBaseUri()).isEqualTo("http://{host}/{path}");
        softly.assertThat(raml.getBasePath()).isEqualTo("/{path}");
        softly.assertThat(raml.getBaseUriParameters()).containsKeys("host", "path");
        softly.assertThat(raml.getProtocols()).isEqualTo(asList(HTTPS));
    }

    // TODO Q: what should we do with the DEFAULT Scheme?

    @Test
    public void shouldIgnoreSchemesUnknownInRAML() {
        @SwaggerDefinition(schemes = { Scheme.HTTP, Scheme.HTTPS, Scheme.WS, Scheme.WSS })
        class Dummy {}

        Raml raml = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(raml.getProtocols()).isEqualTo(asList(HTTP, HTTPS));
    }

    // TODO List<Map<String, String>> schemas = new ArrayList<Map<String, String>>();
    // TODO List<Map<String, Template>> resourceTypes = new ArrayList<Map<String, Template>>();
    // TODO List<Map<String, Template>> traits = new ArrayList<Map<String, Template>>();
    // TODO List<Map<String, SecurityScheme>> securitySchemes = new ArrayList<Map<String, SecurityScheme>>();
    // TODO List<SecurityReference> securedBy = new ArrayList<SecurityReference>();
    // TODO Map<String, Resource> resources = new LinkedHashMap<String, Resource>();
    // TODO List<DocumentationItem> documentation;
}
