package com.github.t1.ramlap;

import static java.lang.annotation.RetentionPolicy.*;
import static javax.tools.Diagnostic.Kind.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.lang.annotation.Retention;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;
import org.raml.model.parameter.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.*;

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
        Action action = action(raml, path, type);
        return then.assertThat(action);
    }

    private Action action(Raml raml, String path, ActionType type) {
        Resource resource = raml.getResource(path);
        then(resource).as("resource " + path).isNotNull();
        Action action = resource.getAction(type);
        then(action).as("action " + type).isNotNull();
        return action;
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
        // TODO responses : Map<String, Response>
        // TODO is : List<String>
        // TODO protocols : List<Protocol>
        // TODO securedBy : List<SecurityReference>
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
        messages.addAll(type.getMethod("foo").getMessages(WARNING)); // one...
        messages.addAll(type.getMethod("bar").getMessages(WARNING)); // ... or the other
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
        messages.addAll(foo.getMethod("foo").getMessages(WARNING)); // one...
        messages.addAll(bar.getMethod("bar").getMessages(WARNING)); // ... or the other
        assertThat(messages).containsExactly("path not unique");
    }

    @Test
    public void shouldSkipContextParam() {
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getEnum(@Context UriInfo uriInfo) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams).isEmpty();
        assertThat(env.getMessager().getMessages()).isEmpty();
    }

    @Test
    public void shouldScanPathParams() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/{s}/{i}-{d}")
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("s") String s, //
                    @JavaDoc(summary = "i-name", value = "i-desc") @PathParam("i") int i, //
                    @JavaDoc(summary = "d-name", value = "d-desc") @PathParam("d") double d //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo/{s}/{i}-{d}", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams.size()).isEqualTo(3);
        then(pathParams.get("s")) //
                .hasDisplayName("s") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isRequired() //
        // TODO repeat : boolean
        // TODO enumeration : List<String>
        // TODO pattern : String
        // TODO minLength : Integer
        // TODO maxLength : Integer
        // TODO minimum : BigDecimal
        // TODO maximum : BigDecimal
        // TODO defaultValue : String
        // TODO example : String
        ;
        then(pathParams.get("i")) //
                .hasDisplayName("i-name") //
                .hasDescription("i-desc") //
                .hasType(INTEGER) //
                .isRequired() //
                ;
        then(pathParams.get("d")) //
                .hasDisplayName("d-name") //
                .hasDescription("d-desc") //
                .hasType(NUMBER) //
                .isRequired() //
                ;
        // TODO Type: DATE
        // TODO Type: FILE

        assertThat(env.getMessager().getMessages()).isEmpty();
    }

    @Test
    public void shouldMarkWarningsWhenPathParamNamesDontMatch() {
        @Path("")
        class Dummy {
            @GET
            @Path("/{foo}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("bar") String bar) {}
        }

        RamlScanner scanner = new RamlScanner();
        ReflectionType type = env.type(Dummy.class);

        scanner.scanJaxRsType(type);

        ReflectionMethod method = type.getMethod("getMethod");
        assertThat(method.getMessages(WARNING)) //
                .containsExactly("no path param annotated as 'foo' found, but required in path '/{foo}'") //
                ;
        assertThat(method.getParameter(0).getMessages(WARNING)) //
                .containsExactly("annotated path param name 'bar' not defined in path '/{foo}'") //
                ;

        assertThat(env.getMessager().getMessages()).hasSize(2);
    }

    @Test
    public void shouldScanQueryParams() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod( //
                    @QueryParam("q0") String q0, //
                    @JavaDoc(summary = "q-name", value = "q-desc") @QueryParam("q1") long q1 //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, QueryParameter> queryParams = action.getQueryParameters();
        assertThat(queryParams.size()).isEqualTo(2);
        then(queryParams.get("q0")) //
                .hasDisplayName("q0") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isNotRequired() //
                ;
        then(queryParams.get("q1")) //
                .hasDisplayName("q-name") //
                .hasDescription("q-desc") //
                .hasType(INTEGER) //
                .isNotRequired() //
                ;

        assertThat(env.getMessager().getMessages()).isEmpty();
    }

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryANDpathParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @Path("/{q}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("q") @QueryParam("q") String q) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p/{q}", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams.size()).isEqualTo(1);
        then(pathParams.get("q")) //
                .hasDisplayName("q") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isRequired() //
                ;
        Map<String, QueryParameter> queryParams = action.getQueryParameters();
        assertThat(queryParams.size()).isEqualTo(1);
        then(queryParams.get("q")) //
                .hasDisplayName("q") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isNotRequired() //
                ;

        assertThat(env.type(Dummy.class).getMethod("getMethod").getParameter(0).getMessages(WARNING)) //
                .containsExactly("method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter") //
                        ;
        assertThat(env.getMessager().getMessages()).hasSize(1);
    }

    // TODO headers : Map<String, Header>
    // TODO body : Map<String, MimeType>
    // TODO cookie
    // TODO form
    // TODO bean
}
