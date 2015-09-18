package com.github.t1.ramlap;

import static com.github.t1.ramlap.StringTools.*;
import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.ws.rs.*;

import org.raml.model.*;
import org.raml.model.parameter.Header;
import org.slf4j.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;
import com.github.t1.ramlap.ResponseScanner.ResponseHeaderScanner;

import io.swagger.annotations.ApiOperation;

public class MethodScanner {
    private static final Logger log = LoggerFactory.getLogger(MethodScanner.class);

    private final Raml raml;
    private final Method method;
    private final ProcessingEnvironment env;

    private Resource resource;
    private Action action;

    public MethodScanner(Raml raml, Method method) {
        this.raml = raml;
        this.method = method;
        this.env = method.getProcessingEnv();
    }

    public void scan() {
        ActionType actionType = actionType();
        if (actionType == null)
            return;
        log.debug("scan {} method {}", actionType, method);
        this.resource = resource();
        this.action = action(actionType);

        action.setDisplayName(displayName());
        action.setDescription(description());

        scanParams();
        scanResponses();
    }

    private Action action(ActionType actionType) {
        Action action = resource.getAction(actionType);
        if (action == null) {
            action = new Action();
            action.setType(actionType);
            action.setResource(resource);
            resource.getActions().put(actionType, action);
        } else {
            method.note("path not unique");
        }
        return action;
    }

    private ActionType actionType() {
        for (AnnotationType annotation : method.getAnnotationTypes())
            if (annotation.getAnnotation(HttpMethod.class) != null)
                return ActionType.valueOf(annotation.getAnnotation(HttpMethod.class).value());
        return null;
    }

    private Resource resource() {
        ResourcePath methodPath = methodPath();
        Resource resource = raml.getResource(methodPath.toString());
        if (resource == null) {
            resource = new Resource();
            methodPath.setResource(raml, resource);
        }
        return resource;
    }

    private ResourcePath methodPath() {
        ResourcePath path = ResourcePath.of(method.getType());
        if (method.isAnnotated(Path.class))
            path = path.and(method.getAnnotation(Path.class).value());
        return path;
    }

    private String displayName() {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null && !apiOperation.value().isEmpty())
            return apiOperation.value();
        if (method.isAnnotated(JavaDoc.class))
            return method.getAnnotation(JavaDoc.class).summary();
        return camelCaseToWords(method.getSimpleName());
    }

    private String description() {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null && !apiOperation.notes().isEmpty())
            return apiOperation.notes();
        return (method.isAnnotated(JavaDoc.class)) ? method.getAnnotation(JavaDoc.class).value() : null;
    }

    private void scanParams() {
        for (Parameter parameter : method.getParameters())
            new ParameterScanner(raml, action, parameter).scan();
        markUnresolvedUriParams();
    }

    private void markUnresolvedUriParams() {
        Set<String> foundVars = action.getResource().getResolvedUriParameters().keySet();
        for (ResourcePathVariable expectedVar : ResourcePath.of(action.getResource().getUri()).vars())
            if (!foundVars.contains(expectedVar.getName()))
                method.warning("no path param annotated as '" + expectedVar.getName() + "' found, but required in "
                        + action.getType() + " of '" + resource.getUri() + "'");
    }

    private void scanResponses() {
        for (ResponseScanner responseScanner : ResponseScanner.responses(method)) {
            String status = responseScanner.status();
            Response response = new Response();
            action.getResponses().put(status, response);

            response.setDescription(responseScanner.description());
            scanHeaders(responseScanner.responseHeaders(), response.getHeaders());
            scanBody(responseScanner.responseType(), response);
        }
    }

    private void scanHeaders(List<ResponseHeaderScanner> scanners, Map<String, Header> map) {
        for (ResponseHeaderScanner scanner : scanners) {
            Header header = new Header();
            header.setDisplayName(scanner.name());
            header.setDescription(scanner.description());
            header.setType(typeInfo(scanner.response()).paramType());
            map.put(scanner.name(), header);
        }
    }

    private void scanBody(Type responseType, Response response) {
        Map<String, MimeType> bodyMap = response.getBody();
        if (bodyMap == null) {
            bodyMap = new HashMap<>();
            response.setBody(bodyMap);
        }

        TypeInfo typeInfo = new TypeInfo(responseType);
        for (String mediaType : produces()) {
            MimeType mimeType = new MimeType();
            mimeType.setType(typeInfo.type());
            mimeType.setSchema(typeInfo.schema(mediaType));
            bodyMap.put(mediaType, mimeType);
        }
    }

    private TypeInfo typeInfo(Class<?> returnType) {
        return new TypeInfo(new ReflectionType(env, returnType));
    }

    private String[] produces() {
        if (method.isAnnotated(Produces.class))
            return method.getAnnotation(Produces.class).value();
        if (raml.getMediaType() != null)
            return new String[] { raml.getMediaType() };
        return new String[] { APPLICATION_JSON };
    }
}
