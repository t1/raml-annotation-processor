package com.github.t1.ramlap;

import static java.lang.annotation.RetentionPolicy.*;
import static javax.tools.Diagnostic.Kind.*;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;

import java.lang.annotation.Retention;
import java.util.*;

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
    private final ReflectionProcessingEnvironment env = new ReflectionProcessingEnvironment();

    @Rule
    public final JUnitSoftAssertions then = new JUnitSoftAssertions();

    private Raml scanTypes(Class<?>... containers) {
        RamlScanner scanner = new RamlScanner();
        for (Class<?> container : containers) {
            Type type = env.type(container);
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
    public void shouldScanPOST() {
        @Path("/foo")
        class Dummy {
            @POST
            public void postMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", POST) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(POST) //
                .hasDisplayName("post method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanPUT() {
        @Path("/foo")
        class Dummy {
            @PUT
            public void putMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", PUT) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(PUT) //
                .hasDisplayName("put method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanDELETE() {
        @Path("/foo")
        class Dummy {
            @DELETE
            public void deleteMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", DELETE) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(DELETE) //
                .hasDisplayName("delete method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanHEAD() {
        @Path("/foo")
        class Dummy {
            @HEAD
            public void headMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", HEAD) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(HEAD) //
                .hasDisplayName("head method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanOPTIONS() {
        @Path("/foo")
        class Dummy {
            @OPTIONS
            public void optionsMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", OPTIONS) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(OPTIONS) //
                .hasDisplayName("options method") //
                .hasDescription(null) //
                ;
    }

    @Retention(RUNTIME)
    @HttpMethod("TRACE")
    public @interface TRACE {}

    @Test
    public void shouldScanTRACE() {
        @Path("/foo")
        class Dummy {
            @TRACE
            public void traceMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", TRACE) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(TRACE) //
                .hasDisplayName("trace method") //
                .hasDescription(null) //
                ;
    }

    @Retention(RUNTIME)
    @HttpMethod("PATCH")
    public @interface PATCH {}

    @Test
    public void shouldScanPATCH() {
        @Path("/foo")
        class Dummy {
            @PATCH
            public void patchMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThatAction(raml, "/foo", PATCH) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(PATCH) //
                .hasDisplayName("patch method") //
                .hasDescription(null) //
                ;
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
        class RootResource {
            @GET
            @Path("/foo")
            public void foo() {}

            @GET
            @Path("/bar")
            public void bar() {}
        }

        Raml raml = scanTypes(RootResource.class);

        assertThat(raml.getResources()).containsOnlyKeys("/root");
        Resource root = raml.getResource("/root");
        then(root) //
                .hasDisplayName("root resource") //
                .hasDescription(null) //
                ;
        assertThat(root.getResources()).containsOnlyKeys("/foo", "/bar");
        then(raml.getResource("/root/foo")).hasParentResource(root);
        then(raml.getResource("/root/foo").getAction(GET)) //
                .hasDisplayName("foo") //
                .hasDescription(null) //
                ;
        then(raml.getResource("/root/bar")).hasParentResource(root);
        then(raml.getResource("/root/bar").getAction(GET)) //
                .hasDisplayName("bar") //
                .hasDescription(null) //
                ;
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
                .hasParentUri("") //
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
                .hasParentUri("") //
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
                .hasParentUri("") //
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
                .hasParentUri("") //
                ;
    }

    @Test
    public void shouldMarkWarningWhenTwoGETsHaveTheSamePath() {
        @Path("/foo")
        class Foo {
            @GET
            @Path("/bar")
            public void foo() {}

            @GET
            @Path("/bar")
            public void bar() {}
        }
        RamlScanner scanner = new RamlScanner();
        ReflectionType type = env.type(Foo.class);

        scanner.scanJaxRsType(type);

        List<String> messages = new ArrayList<>();
        messages.addAll(type.getMethod("foo").getMessages(WARNING));
        messages.addAll(type.getMethod("bar").getMessages(WARNING));
        assertThat(messages).containsExactly("path not unique");
    }

    @Test
    public void shouldMarkWarningWhenTwoGETsHaveTheSamePathInDifferentTypes() {
        @Path("/foo")
        class Foo {
            @GET
            public void foo() {}
        }
        @Path("/")
        class Bar {
            @GET
            @Path("/foo")
            public void bar() {}
        }
        RamlScanner scanner = new RamlScanner();
        ReflectionType foo = env.type(Foo.class);
        scanner.scanJaxRsType(foo);
        ReflectionType bar = env.type(Bar.class);
        scanner.scanJaxRsType(bar);

        List<String> messages = new ArrayList<>();
        messages.addAll(foo.getMethod("foo").getMessages(WARNING));
        messages.addAll(bar.getMethod("bar").getMessages(WARNING));
        assertThat(messages).containsExactly("path not unique");
    }
}
