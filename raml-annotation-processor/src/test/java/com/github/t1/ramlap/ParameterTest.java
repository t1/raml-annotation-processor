package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.Message.*;
import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static com.github.t1.ramlap.Pojo.*;
import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.nio.file.AccessMode;
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
public class ParameterTest extends AbstractTest {
    @Test
    public void shouldSkipContextParam() {
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void get(@Context UriInfo uriInfo) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams).isEmpty();
    }

    @Test
    public void shouldScanPathParams() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/{s}/{i}-{d}-{e}")
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("s") String s, //
                    @JavaDoc(value = "i-name. i-desc") @PathParam("i") int i, //
                    @JavaDoc(value = "d-name. d-desc") @PathParam("d") double d, //
                    @PathParam("e") AccessMode e //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/foo/{s}/{i}-{d}-{e}", GET);
        then(raml.getResource("/foo/{s}").getUriParameters().get("s")) //
                .hasDisplayName("s") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isRequired() //
        // TODO required=false : boolean
        // TODO repeat : boolean
        // TODO pattern : String
        // TODO minLength : Integer
        // TODO maxLength : Integer
        // TODO minimum : BigDecimal
        // TODO maximum : BigDecimal
        // TODO defaultValue : String
        ;
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams.size()).isEqualTo(3);
        then(pathParams.get("i")) //
                .hasDisplayName("i-name") //
                .hasDescription("i-name. i-desc") //
                .hasType(INTEGER) //
                .isRequired() //
                ;
        then(pathParams.get("d")) //
                .hasDisplayName("d-name") //
                .hasDescription("d-name. d-desc") //
                .hasType(NUMBER) //
                .isRequired() //
                ;
        then(pathParams.get("e")) //
                .hasDisplayName("e") //
                .hasDescription(null) //
                .hasType(STRING) //
                .hasEnumeration("EXECUTE", "READ", "WRITE") //
                .isRequired() //
                ;
        // TODO Type: DATE
        // TODO Type: FILE
        // TODO Named Parameters With Multiple Types
    }

    @Test
    public void shouldStripRegexFromPathParam() {
        @Path("")
        class Dummy {
            @GET
            @Path("/{p:.*}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("p") String p) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/{p}", GET);
        Map<String, UriParameter> pathParams = action.getResource().getUriParameters();
        assertThat(pathParams.size()).isEqualTo(1);
        then(pathParams.get("p")) //
                .hasDisplayName("p") //
                .hasType(STRING) //
                ;
    }

    @Test
    public void shouldScanApiPathParam() {
        @Path("")
        class Dummy {
            @GET
            @Path("/{p}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("p") @ApiParam(value = "p-desc") String p) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/{p}", GET);
        Map<String, UriParameter> params = action.getResource().getUriParameters();
        assertThat(params.size()).isEqualTo(1);
        then(params.get("p")) //
                .hasDisplayName("p") //
                .hasType(STRING) //
                .hasDescription("p-desc") //
        // TODO name?
        // TODO defaultValue
        // TODO allowableValues
        // TODO required
        // TODO access
        // TODO allowMultiple
        // TODO hidden
        ;
    }

    @ApiModel(value = "foo-num", description = "foo-num-desc")
    enum FooNum {
        one,
        two,
        three
    }

    @Test
    public void shouldScanApiModelParam() {
        @Path("")
        class Dummy {
            @GET
            @Path("/{p}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("p") FooNum p) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/{p}", GET);
        Map<String, UriParameter> params = action.getResource().getUriParameters();
        assertThat(params.size()).isEqualTo(1);
        then(params.get("p")) //
                .hasType(STRING) //
                .hasDisplayName("foo-num") //
                .hasDescription("foo-num-desc") //
        // TODO parent
        // TODO discriminator
        // TODO subTypes
        // TODO reference
        ;
    }

    @Test
    public void shouldScanSeparateSuperPathParams() {
        @Path("/{foo}")
        class Dummy {
            @GET
            @Path("/{bar}")
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("foo") String foo, //
                    @PathParam("bar") String bar //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThat(raml.getResources()).containsOnlyKeys("/{foo}");
        assertThat(raml.getResource("/{foo}").getUriParameters()).containsOnlyKeys("foo");
        assertThat(raml.getResource("/{foo}").getResources()).containsOnlyKeys("/{bar}");
        assertThat(raml.getResource("/{foo}/{bar}").getUriParameters()).containsOnlyKeys("bar");
    }

    @Test
    public void shouldScanCombinedSuperPathParams() {
        @Path("/{foo}/{bar}")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod( //
                    @PathParam("foo") String foo, //
                    @PathParam("bar") String bar //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        assertThat(raml.getResources()).containsOnlyKeys("/{foo}");
        assertThat(raml.getResource("/{foo}").getResources()).containsOnlyKeys("/{bar}");
        assertThat(raml.getResource("/{foo}").getUriParameters()).containsOnlyKeys("foo");
        assertThat(raml.getResource("/{foo}/{bar}").getUriParameters()).containsOnlyKeys("bar");
    }

    @Test
    public void shouldMarkWarningsWhenPathParamNameIsMissing() {
        @Path("/{foo}")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        RamlScanner scanner = new RamlScanner();
        Type type = ENV.type(Dummy.class);

        scanner.scanJaxRsType(type);

        Method method = type.getMethod("getMethod");
        assertMessage(WARNING, method, "no path param annotated as 'foo' found, but required in GET of '/{foo}'");
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

        Raml raml = scanTypes(Dummy.class);
        assertEquals(asSet("bar"), raml.getResource("/{foo}").getUriParameters().keySet());

        Method method = ENV.type(Dummy.class).getMethod("getMethod");
        assertMessage(WARNING, method, "no path param annotated as 'foo' found, but required in GET of '/{foo}'");
        assertMessage(WARNING, method.getParameter(0),
                "annotated path param name 'bar' not defined in GET of '/{foo}'");
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
                    @JavaDoc(value = "q-name. q-desc") @QueryParam("q1") long q1, //
                    @QueryParam("q2") AccessMode q2 //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, QueryParameter> queryParams = action.getQueryParameters();
        assertThat(queryParams.size()).isEqualTo(3);
        then(queryParams.get("q0")) //
                .hasDisplayName("q0") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isNotRequired() //
                ;
        then(queryParams.get("q1")) //
                .hasDisplayName("q-name") //
                .hasDescription("q-name. q-desc") //
                .hasType(INTEGER) //
                .isNotRequired() //
                ;
        then(queryParams.get("q2")) //
                .hasDisplayName("q2") //
                .hasDescription(null) //
                .hasType(STRING) //
                .hasEnumeration("EXECUTE", "READ", "WRITE") //
                .isNotRequired() //
                ;
    }

    @Test
    public void shouldScanHeaderParams() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod( //
                    @HeaderParam("h0") String h0, //
                    @JavaDoc(value = "h-name. h-desc") @HeaderParam("h1") long h1 //
            ) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, Header> headerParams = action.getHeaders();
        assertThat(headerParams.size()).isEqualTo(2);
        then(headerParams.get("h0")) //
                .hasDisplayName("h0") //
                .hasDescription(null) //
                .hasType(STRING) //
                .isNotRequired() //
                ;
        then(headerParams.get("h1")) //
                .hasDisplayName("h-name") //
                .hasDescription("h-name. h-desc") //
                .hasType(INTEGER) //
                .isNotRequired() //
                ;
    }

    @Test
    public void shouldScanStringBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes(APPLICATION_JSON)
            public void getMethod(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(1);
        then(body.get(APPLICATION_JSON)) //
                .hasType("string") //
                .hasSchema(null) //
                .hasExample(null) //
                ;
        // TODO form params
    }

    @Test
    @Ignore("removing isSimple() from TypeInfo#example throws EmptyStackException")
    public void shouldScanStringBodyParamAnnotatedAsApiExample() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes(APPLICATION_JSON)
            public void getMethod(@ApiExample("sample") String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(1);
        then(body.get(APPLICATION_JSON)) //
                .hasType("string") //
                .hasSchema(null) //
                .hasExample("sample") //
                ;
        // TODO form params
    }

    @Test
    public void shouldScanWildcardBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body).containsOnlyKeys(APPLICATION_JSON);
    }

    @Test
    public void shouldScanMethodsWithTwoMimeTypes() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes({ APPLICATION_JSON, APPLICATION_XML })
            public void getMethod(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        assertThat(body).containsOnlyKeys(APPLICATION_JSON, APPLICATION_XML);
    }

    @Test
    public void shouldScanTwoMethodsWithDifferentMimeTypes() {
        @Api
        @SuppressWarnings("unused")
        @Path("/p")
        class Dummy {
            @GET
            @Consumes(APPLICATION_JSON)
            public void getJson(String body) {}

            @GET
            @Consumes(APPLICATION_XML)
            public void getXml(String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        assertThat(body).containsOnlyKeys(APPLICATION_JSON, APPLICATION_XML);

        assertMessage(NOTE, ANY_ELEMENT, "path not unique");
    }

    @Test
    public void shouldScanPojoBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes({ APPLICATION_JSON, APPLICATION_XML })
            public void getMethod(Pojo body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        then(body.get(APPLICATION_JSON)) //
                .hasType(null) //
                .hasSchema(POJO_JSON_SCHEMA) //
                .hasExample("{" //
                        + "\n    \"value\":\"example-value\"\n" //
                        + "}\n") //
                        ;
        then(body.get(APPLICATION_XML)) //
                .hasType(null) //
                .hasSchema(POJO_XML_SCHEMA) //
                .hasExample(null) // TODO xml example
                ;
        // TODO form params
        assertThat(raml.getSchemas()).hasSize(0);
    }

    // TODO cookie-param
    // TODO form-param
    // TODO bean-param

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryAndPathParam() {
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
        assertThat(action.getResource().getUriParameters().size()).isEqualTo(1);
        assertThat(action.getQueryParameters().size()).isEqualTo(1);

        assertMessage(WARNING, ENV.type(Dummy.class).getMethod("getMethod").getParameter(0), //
                "method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryAndHeaderParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod(@QueryParam("q") @HeaderParam("q") String q) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        assertThat(action.getHeaders().size()).isEqualTo(1);
        assertThat(action.getQueryParameters().size()).isEqualTo(1);

        assertMessage(WARNING, ENV.type(Dummy.class).getMethod("getMethod").getParameter(0),
                "method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    @Test
    public void shouldMarkWarningIfAParameterIsMarkedAsQueryAndPathAndHeaderParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @Path("/{q}")
            @SuppressWarnings("unused")
            public void getMethod(@PathParam("q") @QueryParam("q") @HeaderParam("q") String q) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p/{q}", GET);
        assertThat(action.getResource().getUriParameters().size()).isEqualTo(1);
        assertThat(action.getQueryParameters().size()).isEqualTo(1);
        assertThat(action.getHeaders().size()).isEqualTo(1);

        assertMessage(WARNING, ENV.type(Dummy.class).getMethod("getMethod").getParameter(0),
                "method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    @Test
    public void testName() {
        System.out.println(new Random().nextInt(9999));
    }
}
