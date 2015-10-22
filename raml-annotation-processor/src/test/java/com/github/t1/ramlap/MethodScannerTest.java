package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.Message.*;
import static com.github.t1.ramlap.Pojo.*;
import static com.github.t1.ramlap.ProblemDetail.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.lang.annotation.Retention;
import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.Response.StatusType;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.Type;

import io.swagger.annotations.*;

@RunWith(MockitoJUnitRunner.class)
public class MethodScannerTest extends AbstractScannerTest {
    public static final class FooBadRequest extends ProblemDetail {}

    @ApiResponse(status = NOT_FOUND, title = "foo-nf")
    public static final class FooNotFound extends ProblemDetail {}

    private ProblemDetail catchProblemDetail(StatusType expectedStatus, ThrowingCallable callable) {
        WebApplicationApplicationException throwable = (WebApplicationApplicationException) catchThrowable(callable);
        assertThat(throwable.getResponse().getStatusInfo()).isEqualTo(expectedStatus);
        return ProblemDetail.of(throwable);
    }

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
    public void shouldScanUnspecificResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema(null) //
                ;
    }

    @Test
    public void shouldScanImplicitPojoResponse() {
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
                .hasSchema(POJO_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanExplicitPojoResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "ok-descr", type = Pojo.class)
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema(POJO_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanExplicitPojoResponseWithStatusCode() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(statusCode = 793, title = "zombie apocalypse")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("793");
        then(response).hasDescription("zombie apocalypse").doesNotHaveBody();
    }

    @Test
    public void shouldScanProblemDetailAnnotation() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(type = FooNotFound.class)
            public void getMethod() {
                throw new FooNotFound().detail("detail-text").toWebException();
            }
        }

        assertFooNotFound(catchProblemDetail(NOT_FOUND, () -> new Dummy().getMethod()));

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response notFoundResponse = action.getResponses().get("404");
        assertThat(notFoundResponse.getBody()).hasSize(1);
        then(notFoundResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(APPLICATION_PROBLEM_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanTwoProblemDetailResponses() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(type = FooBadRequest.class)
            @ApiResponse(type = FooNotFound.class)
            public javax.ws.rs.core.Response getMethod(boolean b) {
                return b //
                        ? new FooBadRequest().toResponse() : new FooNotFound().detail("detail-text").toResponse();
            }
        }

        assertFooBadRequest((ProblemDetail) new Dummy().getMethod(true).getEntity());
        assertFooNotFound((ProblemDetail) new Dummy().getMethod(false).getEntity());

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);

        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(APPLICATION_PROBLEM_JSON_SCHEMA) //
                ;

        Response notFoundResponse = action.getResponses().get("404");
        assertThat(notFoundResponse.getBody()).hasSize(1);
        then(notFoundResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(APPLICATION_PROBLEM_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldThrowProblemDetailResponse() {
        class Dummy {
            public void getMethod(boolean b) {
                if (b)
                    throw new FooBadRequest().toWebException();
                throw new FooNotFound().detail("detail-text").toWebException();
            }
        }

        assertFooBadRequest(catchProblemDetail(BAD_REQUEST, () -> new Dummy().getMethod(true)));
        assertFooNotFound(catchProblemDetail(NOT_FOUND, () -> new Dummy().getMethod(false)));
    }

    private void assertFooBadRequest(ProblemDetail problemDetail) {
        assertThat(problemDetail.type()).isEqualTo(URI.create("urn:problem:java:" + FooBadRequest.class.getName()));
        assertThat(problemDetail.title()).isEqualTo("foo bad request");
        assertThat(problemDetail.status()).isEqualTo(BAD_REQUEST);
        assertThat(problemDetail.detail()).isNull();
        assertThat(problemDetail.instance().toString())
                .matches("urn:problem-instance:........-....-....-....-............");
    }

    private void assertFooNotFound(ProblemDetail problemDetail) {
        assertThat(problemDetail.type()).isEqualTo(URI.create("urn:problem:java:" + FooNotFound.class.getName()));
        assertThat(problemDetail.title()).isEqualTo("foo-nf");
        assertThat(problemDetail.status()).isEqualTo(NOT_FOUND);
        assertThat(problemDetail.detail()).isEqualTo("detail-text");
        assertThat(problemDetail.instance().toString())
                .matches("urn:problem-instance:........-....-....-....-............");
    }

    @Test
    public void shouldWarnAboutStatusCodeAndStatusEnumValue() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = UNAUTHORIZED, statusCode = 201, title = "created")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("201");
        then(response).hasDescription("created").doesNotHaveBody();

        assertMessage(ERROR, Type.of(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Conflicting specification of status Unauthorized and status code 201. You should just use the status.");
    }

    @Test
    public void shouldWarnAboutStatusCodeForDefinedDefaultStatusEnumValue() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(statusCode = 201, title = "okay")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("201");
        then(response).hasDescription("okay").doesNotHaveBody();

        assertMessage(WARNING, Type.of(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Status code 201 is defined as Created. You should use that instead.");
    }

    @Test
    public void shouldWarnAboutStatusCodeForDefinedStatusEnumValue() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(statusCode = 201, title = "okay")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("201");
        then(response).hasDescription("okay").doesNotHaveBody();

        assertMessage(WARNING, Type.of(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Status code 201 is defined as Created. You should use that instead.");
    }

    @Test
    public void shouldWarnAboutStatusCodeForDefinedStatusEnumValueEvenWhenStatusIsSpecifiedAsWell() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = CREATED, statusCode = 201, title = "okay")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("201");
        then(response).hasDescription("okay").doesNotHaveBody();

        assertMessage(WARNING, Type.of(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Status code 201 is defined as Created. You should use that instead.");
    }

    @Test
    public void shouldScanExplicitPojoSwaggerResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr", response = Pojo.class)
            public Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema(POJO_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanTwoResponsesWithOneProblemDetailResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "ok-descr")
            @ApiResponse(status = BAD_REQUEST, title = "bad-request-descr")
            public FooBadRequest getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);

        Response okResponse = action.getResponses().get("200");
        assertThat(okResponse.getBody()).hasSize(1);
        then(okResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(APPLICATION_PROBLEM_JSON_SCHEMA) //
                ;

        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(APPLICATION_PROBLEM_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanTwoSwaggerResponses() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponses({ //
                    @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr"),
                    @io.swagger.annotations.ApiResponse(code = 400, message = "bad-request-descr") //
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
                .hasSchema(POJO_JSON_SCHEMA) //
                ;

        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse.getBody().get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema(POJO_JSON_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanIntegerJsonResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "ok-descr", type = Integer.class)
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
    public void shouldScanIntegerJsonSwaggerResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr", response = Integer.class)
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
            @ApiResponse(status = OK, title = "ok-descr")
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
                .hasSchema(POJO_JSON_SCHEMA) //
                ;
        then(response.getBody().get(APPLICATION_XML)) //
                .hasType(null) //
                .hasSchema(POJO_XML_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanJsonAndXmlSwaggerResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr")
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
                .hasSchema(POJO_JSON_SCHEMA) //
                ;
        then(response.getBody().get(APPLICATION_XML)) //
                .hasType(null) //
                .hasSchema(POJO_XML_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanXmlResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "ok-descr")
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
                .hasSchema(POJO_XML_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanXmlResponseSwagger() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr")
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
                .hasSchema(POJO_XML_SCHEMA) //
                ;
    }

    @Test
    public void shouldScanFallbackMediaTypeResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "ok-descr", type = Integer.class)
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
    public void shouldScanFallbackMediaTypeSwaggerResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr", response = Integer.class)
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
        // we have three response types here: String, boolean, and Integer... the last one is correct
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "ok-descr", type = boolean.class, //
                    responseHeaders = @ApiResponseHeader(name = "resp-header", description = "r-h-desc",
                            type = Integer.class) //
            )
            public String getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);

        assertThat(action.getResponses()).hasSize(1);

        Response response = action.getResponses().get("200");
        then(response).hasDescription("ok-descr");
        assertThat(response.getHeaders()).containsOnlyKeys("resp-header");
        then(response.getHeaders().get("resp-header")) //
                .hasDisplayName("resp-header") //
                .hasDescription("r-h-desc") //
                .hasType(INTEGER) //
                ;
    }

    @Test
    public void shouldScanSwaggerResponseHeader() {
        // we have three response types here: String, boolean, and Integer... the last one is correct
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr", response = boolean.class, //
                    responseHeaders = @ResponseHeader(name = "resp-header", description = "r-h-desc",
                            response = Integer.class) //
            )
            public String getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);

        assertThat(action.getResponses()).hasSize(1);

        Response response = action.getResponses().get("200");
        then(response).hasDescription("ok-descr");
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
        Type type = Type.of(Foo.class);

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
        Type foo = Type.of(Foo.class);
        scanner.scanJaxRsType(foo);
        Type bar = Type.of(Bar.class);
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
