package com.github.t1.ramlap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;

import javax.ws.rs.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.ApiOperation;

@RunWith(MockitoJUnitRunner.class)
public class MethodScannerTest {
    @Rule
    public final JUnitSoftAssertions then = new JUnitSoftAssertions();

    private Raml scanTypes(Class<?>... containers) {
        RamlScanner scanner = new RamlScanner();
        for (Class<?> container : containers) {
            Type type = new ReflectionType(container);
            scanner.scanJaxRsType(type);
        }
        return scanner.getResult();
    }

    private ActionAssert assertThatAction(Raml raml, String path, ActionType type) {
        Resource resource = raml.getResource(path);
        then(resource).as("resource " + path).isNotNull();
        Action action = resource.getAction(type);
        then(action).as("action " + type).isNotNull();
        return then.assertThat(action);
    }

    @Test
    public void shouldScanGET() {
        @Path("/foo")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", GET) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(GET) //
                .hasDisplayName("get method") //
                .hasDescription(null) //
                ;
        // TODO headers : Map<String, Header>
        // TODO queryParameters : Map<String, QueryParameter>
        // TODO body : Map<String, MimeType>
        // TODO responses : Map<String, Response>
        // TODO is : List<String>
        // TODO protocols : List<Protocol>
        // TODO securedBy : List<SecurityReference>
        // TODO baseUriParameters : Map<String, List<UriParameter>>
    }

    @Test
    public void shouldScanGETwithJavaDoc() {
        @Path("/foo")
        class Dummy {
            @JavaDoc(summary = "summary", value = "full")
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", GET) //
                .hasDisplayName("summary") //
                .hasDescription("full") //
                ;
    }

    @Test
    public void shouldScanGETwithApiOperation() {
        @Path("/foo")
        class Dummy {
            @ApiOperation(value = "summary", notes = "full"
            // 'tags' do not exist in RAML
            // TODO response
            // TODO responseContainer
            // TODO responseReference
            // TODO httpMethod
            // TODO position
            // TODO nickname
            // TODO produces
            // TODO consumes
            // TODO protocols
            // TODO authorizations
            // TODO hidden
            // TODO responseHeaders
            // TODO code
            // 'extensions' do not exist in RAML
            )
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", GET) //
                .hasDisplayName("summary") //
                .hasDescription("full") //
                ;
    }

    @Test
    public void shouldScanTwoMethodsWithSameRoot() {
        @Path("/root")
        class Root {
            @GET
            @Path("/foo")
            public void foo() {}

            @GET
            @Path("/bar")
            public void bar() {}
        }

        Raml raml = scanTypes(Root.class);

        assertThat(raml.getResources()).containsOnlyKeys("/root");
        Resource root = raml.getResource("/root");
        assertThat(root).isNotNull();
        assertThat(root.getResources()).containsOnlyKeys("/foo", "/bar");
    }

    @Test
    public void shouldConcatenatePathsWithAllSlashes() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/foo/bar")) //
                .hasParentUri("/foo") //
                .hasRelativeUri("/bar") //
                ;
    }

    @Test
    public void shouldConcatenatePathsNonSlashAndSlash() {
        @Path("foo")
        class Dummy {
            @GET
            @Path("/bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/foo/bar")) //
                .hasParentUri("/foo") //
                .hasRelativeUri("/bar") //
                ;
    }

    @Test
    public void shouldConcatenatePathsSlashAndNonSlash() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/foo/bar")) //
                .hasParentUri("/foo") //
                .hasRelativeUri("/bar") //
                ;
    }

    @Test
    public void shouldConcatenatePathsNonSlashAndNonSlash() {
        @Path("foo")
        class Dummy {
            @GET
            @Path("bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/foo/bar")) //
                .hasParentUri("/foo") //
                .hasRelativeUri("/bar") //
                ;
    }

    @Test
    public void shouldConcatenateEmptyPathsToPath() {
        @Path("foo")
        class Dummy {
            @GET
            @Path("")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);


        then.assertThat(raml.getResource("/foo")) //
                .hasRelativeUri("/foo") //
                .hasParentUri(null) //
                ;
    }

    @Test
    public void shouldConcatenatePathToEmptyPath() {
        @Path("")
        class Dummy {
            @GET
            @Path("bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/bar")) //
                .hasRelativeUri("/bar") //
                .hasParentUri(null) //
                ;
    }

    @Test
    public void shouldConcatenateJustSlashToPath() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/foo")) //
                .hasRelativeUri("/foo") //
                .hasParentUri(null) //
                ;
    }

    @Test
    public void shouldConcatenatePathToJustSlash() {
        @Path("/")
        class Dummy {
            @GET
            @Path("bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then.assertThat(raml.getResource("/bar")) //
                .hasRelativeUri("/bar") //
                .hasParentUri(null) //
                ;
    }
}
