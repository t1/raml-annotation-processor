package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.BddAssertions.*;

import javax.ws.rs.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.Type;

@RunWith(MockitoJUnitRunner.class)
public class ResourceTest {
    @Rule
    public final JUnitSoftAssertions then = new JUnitSoftAssertions();

    private Raml scanTypes(Class<?>... containers) {
        RamlScanner scanner = new RamlScanner();
        for (Class<?> container : containers) {
            Type type = ENV.type(container);
            scanner.scanJaxRsType(type);
        }
        return scanner.getResult();
    }

    @Test
    public void shouldScanResource() {
        @Path("/foo/bar")
        class DummyType {
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(DummyType.class);

        then(raml) //
                .hasTitle("") //
                .hasVersion(null);
        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        Resource foo = raml.getResource("/foo");
        then(foo).as("foo") //
                .hasDisplayName(null) //
                .hasDescription(null) //
                .hasParentResource(null) //
                .hasParentUri("") //
                .hasRelativeUri("/foo") //
                .hasUri("/foo") //
                ;
        Resource bar = raml.getResource("/foo/bar");
        then(bar).as("bar") //
                .hasDisplayName("dummy type") //
                .hasDescription(null) //
                // TODO .hasBaseUriParameters(null) //
                // TODO .hasIs(null) // traits
                .hasParentResource(foo) //
                .hasParentUri("/foo") //
                .hasRelativeUri("/bar") //
                .hasUri("/foo/bar") //
        // TODO .hasResolvedUriParameters(null) //
        // TODO .hasResources(null) //
        // TODO .hasSecuredBy(null) //
        // TODO .hasType(null) //
        // TODO .hasUriParameters(null) //
        ;
    }

    @Test
    public void shouldScanResourceJavaDoc() {
        @JavaDoc(value = "first sentence. full doc.")
        @Path("/foo")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Resource resource = raml.getResource("/foo");
        then(resource) //
                .hasDisplayName("first sentence") //
                .hasDescription("first sentence. full doc.") //
                ;
    }

    @Test
    public void shouldScanTwoResourcesWithSameRoot() {
        @Path("/root/foo")
        class Foo {
            @GET
            public void foo() {}
        }
        @Path("/root/bar")
        class Bar {
            @GET
            public void bar() {}
        }

        Raml raml = scanTypes(Foo.class, Bar.class);

        assertThat(raml.getResources()).containsOnlyKeys("/root");
        Resource root = raml.getResource("/root");
        assertThat(root).isNotNull();
        assertThat(root.getResources()).containsOnlyKeys("/foo", "/bar");
    }
}
