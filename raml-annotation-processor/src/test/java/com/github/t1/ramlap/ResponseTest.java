package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static com.github.t1.ramlap.Pojo.*;
import static com.github.t1.ramlap.ProblemDetail.*;
import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.math.BigInteger;
import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.Response.StatusType;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;

import com.github.t1.ramlap.ProblemDetail.BadRequest;

import io.swagger.annotations.ResponseHeader;

@RunWith(MockitoJUnitRunner.class)
public class ResponseTest extends AbstractTest {
    public static final class FooBadRequest extends ProblemDetail {}

    @ApiResponse(status = NOT_FOUND, title = "foo-nf")
    public static final class FooNotFound extends ProblemDetail {}

    private ProblemDetail catchProblemDetail(StatusType expectedStatus, ThrowingCallable callable) {
        WebApplicationApplicationException throwable = (WebApplicationApplicationException) catchThrowable(callable);
        assertThat(throwable.getResponse().getStatusInfo()).isEqualTo(expectedStatus);
        return ProblemDetail.of(throwable);
    }

    private String jsonSchema(Class<? extends ProblemDetail> type) {
        return SchemaGenerator.schema(ENV.type(type), APPLICATION_JSON);
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
            @ApiResponse(status = OK, type = Pojo.class)
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
        assertThat(action.getResponses().size()).isEqualTo(1);
        Response notFoundResponse = action.getResponses().get("404");
        then(notFoundResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(jsonSchema(FooNotFound.class)) //
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

        assertThat(action.getResponses().size()).isEqualTo(2);
        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(jsonSchema(FooBadRequest.class)) //
                ;

        Response notFoundResponse = action.getResponses().get("404");
        assertThat(notFoundResponse.getBody()).hasSize(1);
        then(notFoundResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(jsonSchema(FooNotFound.class)) //
                ;
    }

    @Test
    public void shouldScanTwoProblemDetailResponsesWithSameStatus() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(type = FooBadRequest.class)
            @ApiResponse(type = BadRequest.class, title = "bar")
            public void getMethod() {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);

        assertThat(action.getResponses().size()).isEqualTo(1);
        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse).hasDescription("bar");
        then(badRequestResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(jsonSchema(BadRequest.class)) //
                ;
    }

    @Test
    public void shouldScanResponseTypeNameAsTitle() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, type = BigInteger.class)
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response).hasDescription("big integer");
    }

    @Test
    public void shouldScanResponseTitle() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, title = "foo")
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response).hasDescription("foo");
    }

    @Test
    public void shouldScanResponseWithoutDescription() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK)
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response).hasDescription(null);
    }

    @Test
    public void shouldScanResponseDetailWithoutType() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, detail = "foo")
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response).hasDescription("foo");
    }

    @Test
    public void shouldScanResponseDetailWithType() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, type = BigInteger.class, detail = "foo")
            public javax.ws.rs.core.Response getMethod() {
                return null;
            }
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo", GET);
        Response response = action.getResponses().get("200");
        assertThat(response.getBody()).hasSize(1);
        then(response).hasDescription("big integer: foo");
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

        assertMessage(ERROR, ENV.type(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
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

        assertMessage(WARNING, ENV.type(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Status code 201 is defined as 'Created'. You should use the constant instead.");
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

        assertMessage(WARNING, ENV.type(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Status code 201 is defined as 'Created'. You should use the constant instead.");
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

        assertMessage(WARNING, ENV.type(Dummy.class).getMethod("getMethod").getAnnotationWrapper(ApiResponse.class),
                "Status code 201 is defined as 'Created'. You should use the constant instead.");
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
            @ApiResponse(status = OK)
            @ApiResponse(status = BAD_REQUEST)
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
                .hasSchema(jsonSchema(FooBadRequest.class)) //
                ;

        Response badRequestResponse = action.getResponses().get("400");
        assertThat(badRequestResponse.getBody()).hasSize(1);
        then(badRequestResponse.getBody().get(APPLICATION_PROBLEM_JSON)) //
                .hasType(null) //
                .hasSchema(jsonSchema(FooBadRequest.class)) //
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
            @ApiResponse(status = OK, type = Integer.class)
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
            @ApiResponse(status = OK)
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
            @ApiResponse(status = OK)
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
            @ApiResponse(status = OK, type = Integer.class)
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
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponse(status = OK, responseHeaders = { //
                    @ApiResponseHeader(name = "resp-header", description = "r-h-desc", type = Integer.class) //
            })
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

    @Test
    public void shouldScanSwaggerResponseHeader() {
        @Path("/foo")
        class Dummy {
            @GET
            @io.swagger.annotations.ApiResponse(code = 200, message = "ok-descr", responseHeaders = { //
                    @ResponseHeader(name = "resp-header", description = "r-h-desc", response = Integer.class) //
            })
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

    // TODO {?} response header names
    // TODO response schema
    // TODO examples
    // TODO responses : Map<String, Response>
}
