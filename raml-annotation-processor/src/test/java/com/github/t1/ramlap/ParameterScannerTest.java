package com.github.t1.ramlap;

import static javax.tools.Diagnostic.Kind.*;
import static javax.ws.rs.core.MediaType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.raml.model.ActionType.*;
import static org.raml.model.BddAssertions.*;
import static org.raml.model.ParamType.*;

import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.*;
import org.raml.model.parameter.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.Api;

@RunWith(MockitoJUnitRunner.class)
public class ParameterScannerTest extends AbstractScannerTest {
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
        assertMessages(0);
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
        // TODO required=false : boolean
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
        // TODO Named Parameters With Multiple Types

        assertMessages(0);
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

        assertMessages(2);
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

        assertMessages(0);
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
                    @JavaDoc(summary = "h-name", value = "h-desc") @HeaderParam("h1") long h1 //
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
                .hasDescription("h-desc") //
                .hasType(INTEGER) //
                .isNotRequired() //
                ;

        assertMessages(0);
    }

    @Test
    public void shouldScanBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes(APPLICATION_JSON)
            public void getMethod(@JavaDoc(summary = "the body", value = "full of content") String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(1);
        then(body.get(APPLICATION_JSON)) //
                .hasType(APPLICATION_JSON) //
                .hasSchema(null) // TODO
                .hasExample(null) // TODO
                ;
        // TODO form params
        assertMessages(0);
    }

    @Test
    public void shouldScanWildcardBodyParam() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            public void getMethod(@JavaDoc(summary = "the body", value = "full of content") String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(1);
        then(body.get(WILDCARD)).hasType(WILDCARD);
        assertMessages(0);
    }

    @Test
    public void shouldScanMethodsWithTwoMimeTypes() {
        @Api
        @Path("/p")
        class Dummy {
            @GET
            @SuppressWarnings("unused")
            @Consumes({ APPLICATION_JSON, APPLICATION_XML })
            public void getMethod(@JavaDoc(summary = "the body", value = "full of content") String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        then(body.get(APPLICATION_JSON)).hasType(APPLICATION_JSON);
        then(body.get(APPLICATION_XML)).hasType(APPLICATION_XML);
        assertMessages(0);
    }

    @Test
    public void shouldScanTwoMethodsWithDifferentMimeTypes() {
        @Api
        @SuppressWarnings("unused")
        @Path("/p")
        class Dummy {
            @GET
            @Consumes(APPLICATION_JSON)
            public void getJson(@JavaDoc(summary = "the body", value = "full of content") String body) {}

            @GET
            @Consumes(APPLICATION_XML)
            public void getXml(@JavaDoc(summary = "the body", value = "full of content") String body) {}
        }

        Raml raml = scanTypes(Dummy.class);

        Action action = action(raml, "/p", GET);
        Map<String, MimeType> body = action.getBody();
        assertThat(body.size()).isEqualTo(2);
        then(body.get(APPLICATION_JSON)).hasType(APPLICATION_JSON);
        then(body.get(APPLICATION_XML)).hasType(APPLICATION_XML);

        assertMessages(NOTE, "path not unique");
    }

    // TODO cookie
    // TODO form
    // TODO bean

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

        assertThat(env.type(Dummy.class).getMethod("getMethod").getParameter(0).getMessages(WARNING)) //
                .containsExactly("method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter") //
                        ;
        assertMessages(1);
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

        assertThat(env.type(Dummy.class).getMethod("getMethod").getParameter(0).getMessages(WARNING)) //
                .containsExactly("method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter") //
                        ;
        assertMessages(1);
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

        assertThat(env.type(Dummy.class).getMethod("getMethod").getParameter(0).getMessages(WARNING)) //
                .containsExactly("method parameters can be only be annotated as one of " //
                        + "path, query, header, cookie, bean, form, or matrix parameter") //
                        ;
        assertMessages(1);
    }
}
