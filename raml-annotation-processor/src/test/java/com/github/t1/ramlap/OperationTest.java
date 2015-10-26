package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.Message.*;
import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;

import java.lang.annotation.Retention;

import javax.ws.rs.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.Type;

import io.swagger.annotations.ApiOperation;

@RunWith(MockitoJUnitRunner.class)
public class OperationTest extends AbstractTest {
    public static final class FooBadRequest extends ProblemDetail {}

    @Test
    public void shouldScanGET() {
        @Path("/foo")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        then(action) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(GET) //
                .hasDisplayName("get method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanPOST() {
        @Path("/foo")
        class Dummy {
            @POST
            public void postMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then(action(raml, "/foo", POST)) //
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

        then(action(raml, "/foo", PUT)) //
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

        then(action(raml, "/foo", DELETE)) //
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

        then(action(raml, "/foo", HEAD)) //
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

        then(action(raml, "/foo", OPTIONS)) //
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

        then(action(raml, "/foo", TRACE)) //
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

        then(action(raml, "/foo", PATCH)) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(PATCH) //
                .hasDisplayName("patch method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanGETandPUT() {
        @Path("/foo")
        class Dummy {
            @GET
            public void getMethod() {}

            @PUT
            public void putMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        then(action) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(GET) //
                .hasDisplayName("get method") //
                .hasDescription(null) //
                ;

        then(action(raml, "/foo", PUT)) //
                .hasResource(raml.getResource("/foo")) //
                .hasType(PUT) //
                .hasDisplayName("put method") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanTwoSubPaths() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/bar/baz")
            public void getBaz() {}

            @GET
            @Path("/bar/bib")
            public void getBib() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then(action(raml, "/foo/bar/baz", GET)) //
                .hasResource(raml.getResource("/foo/bar/baz")) //
                .hasType(GET) //
                .hasDisplayName("get baz") //
                .hasDescription(null) //
                ;

        then(action(raml, "/foo/bar/bib", GET)) //
                .hasResource(raml.getResource("/foo/bar/bib")) //
                .hasType(GET) //
                .hasDisplayName("get bib") //
                .hasDescription(null) //
                ;
    }

    @Test
    public void shouldScanJavaDoc() {
        @Path("/foo")
        class Dummy {
            @JavaDoc("summary. full")
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then(action(raml, "/foo", GET)) //
                .hasDisplayName("summary") //
                .hasDescription("summary. full") //
                ;
    }

    @Test
    public void shouldScanApiOperation() {
        @Path("/foo")
        class Dummy {
            @ApiOperation(value = "summary", notes = "full"
            // 'tags' do not exist in RAML
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
            // TODO code
            // 'extensions' do not exist in RAML
            )
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then(action(raml, "/foo", GET)) //
                .hasDisplayName("summary") //
                .hasDescription("full") //
                ;
    }

    // TODO {*} request header names
    // TODO replace methodName, resourcePath, resourcePathName, and mediaTypeExtension
    // TODO form params
    // TODO request schema
    // TODO !singularize and !pluralize functions
    // TODO optional properties with ?
    // TODO is : List<String>
    // TODO protocols : List<Protocol>
    // TODO securedBy : List<SecurityReference>

    @Test
    public void shouldScanSubResource() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/bar")
            public void foobar() {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThat(raml.getResources()).containsOnlyKeys("/foo");
        Resource foo = raml.getResource("/foo");
        then(foo) //
                .hasDisplayName("dummy") //
                .hasDescription(null) //
                ;
        assertThat(foo.getResources()).containsOnlyKeys("/bar");
        then(raml.getResource("/foo/bar")).hasParentResource(foo);
        then(raml.getResource("/foo/bar").getAction(GET)) //
                .hasDisplayName("foobar") //
                .hasDescription(null) //
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
    public void shouldWarnAboutWhenTwoGETsHaveTheSamePath() {
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
        Type type = ENV.type(Foo.class);

        scanner.scanJaxRsType(type);

        assertMessage(NOTE, ANY_ELEMENT, "path not unique");
    }

    @Test
    public void shouldWarnAboutWhenTwoGETsHaveTheSamePathInDifferentTypes() {
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
        Type foo = ENV.type(Foo.class);
        scanner.scanJaxRsType(foo);
        Type bar = ENV.type(Bar.class);
        scanner.scanJaxRsType(bar);

        assertMessage(NOTE, ANY_ELEMENT, "path not unique");
    }

    @Test
    public void shouldScanConsumesOnMethod() {
        @Path("/")
        class Dummy {
            @PUT
            @Consumes({ APPLICATION_XML, APPLICATION_JSON })
            public void putMethod(@SuppressWarnings("unused") Integer i) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/", PUT);
        assertThat(action.getBody()).containsOnlyKeys(APPLICATION_XML, APPLICATION_JSON);
        then(action.getBody().get(APPLICATION_JSON)) //
                .hasType("integer") //
                .hasSchema(null) //
                ;
        then(action.getBody().get(APPLICATION_XML)) //
                .hasType("integer") //
                .hasSchema(null) //
                ;
    }

    @Test
    public void shouldScanConsumesOnType() {
        @Path("/")
        @Consumes({ APPLICATION_XML, APPLICATION_JSON })
        class Dummy {
            @PUT
            public void putMethod(@SuppressWarnings("unused") Integer i) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/", PUT);
        assertThat(action.getBody()).containsOnlyKeys(APPLICATION_XML, APPLICATION_JSON);
        // other asserted details are the same as in the test with @Consumes on method
    }

    @Test
    public void shouldScanWithoutConsumesToFallBackOnJson() {
        @Path("/")
        class Dummy {
            @PUT
            public void putMethod(@SuppressWarnings("unused") Integer i) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/", PUT);
        assertThat(action.getBody()).containsOnlyKeys(APPLICATION_JSON);
        // other asserted details are the same as in the test with @Consumes on method
    }
}
