package com.github.t1.ramlap;

import static java.lang.annotation.RetentionPolicy.*;
import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.lang.annotation.Retention;

import javax.ws.rs.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.ReflectionType;

import io.swagger.annotations.*;

@RunWith(MockitoJUnitRunner.class)
public class MethodScannerTest extends AbstractScannerTest {
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
    public void shouldScanJavaDoc() {
        @Path("/foo")
        class Dummy {
            @JavaDoc(summary = "summary", value = "full")
            @GET
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        then(action(raml, "/foo", GET)) //
                .hasDisplayName("summary") //
                .hasDescription("full") //
                ;
    }

    @Test
    public void shouldScanApiOperation() {
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

        then(action(raml, "/foo", GET)) //
                .hasDisplayName("summary") //
                .hasDescription("full") //
                ;
    }

    @Test
    public void shouldScanImplicitResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            public Pojo getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema("!include " + Pojo.class.getName() + ".json") //
                ;
    }

    @Test
    public void shouldScanTwoResponses() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponses({ //
                    @ApiResponse(code = 200, message = "ok-descr"),
                    @ApiResponse(code = 400, message = "bad-request-descr") //
            })
            @Produces(APPLICATION_JSON)
            public Pojo getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);

        Response okResponse = action.getResponses().get("200");
        assertThat(okResponse.getBody()).hasSize(1);
        then(okResponse.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema("!include " + Pojo.class.getName() + ".json") //
                ;

        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema("!include " + Pojo.class.getName() + ".json") //
                ;
    }

    @Test
    public void shouldScanIntegerJsonResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(code = 200, message = "ok-descr", response = Integer.class)
            @Produces(APPLICATION_JSON)
            public String getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType("integer") //
                .hasSchema(null) //
                ;
    }

    @Test
    public void shouldScanJsonAndXmlResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(code = 200, message = "ok-descr")
            @Produces({ APPLICATION_JSON, APPLICATION_XML })
            public Pojo getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(2);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema("!include " + Pojo.class.getName() + ".json") //
                ;
        then(response.getBody().get(APPLICATION_XML)) //
                .hasType(null) //
                .hasSchema("!include " + Pojo.class.getName() + ".xsd") //
                ;
    }

    @Test
    public void shouldScanXmlResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(code = 200, message = "ok-descr")
            @Produces(APPLICATION_XML)
            public Pojo getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_XML)) //
                .hasType(null) //
                .hasSchema("!include " + Pojo.class.getName() + ".xsd") //
                ;
    }

    @Test
    public void shouldScanFallbackMediaTypeResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(code = 200, message = "ok-descr", response = Integer.class)
            public String getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        assertThat(action.getResponses()).hasSize(1);

        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType("integer") //
                .hasSchema(null) //
                ;
    }

    @Test
    public void shouldScanResponseHeader() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(code = 200, message = "ok-descr", response = Long.class,
                    responseHeaders = @ResponseHeader(name = "resp-header", description = "r-h-desc",
                            response = Integer.class) )
            public String getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        assertThat(action.getResponses()).hasSize(1);

        Response response = action.getResponses().get("200");
        assertThat(response.getHeaders()).containsOnlyKeys("resp-header");
        then(response.getHeaders().get("resp-header")) //
                .hasDisplayName("resp-header") //
                .hasDescription("r-h-desc") //
                .hasType(INTEGER) //
                ;
    }

    // TODO {*} request header names
    // TODO {?} response header names
    // TODO replace methodName, resourcePath, resourcePathName, and mediaTypeExtension
    // TODO form params
    // TODO request schema
    // TODO response schema
    // TODO examples
    // TODO !singularize and !pluralize functions
    // TODO optional properties with ?
    // TODO responses : Map<String, Response>
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

        assertMessages(NOTE, "path not unique");
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

        assertMessages(NOTE, "path not unique");
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
