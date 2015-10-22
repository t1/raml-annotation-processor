package com.github.t1.ramlap;

import static com.github.t1.ramlap.StringTools.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import javax.ws.rs.*;

import org.raml.model.*;
import org.raml.model.parameter.Header;
import org.slf4j.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.ApiOperation;

public class MethodScanner {
    private static final Logger log = LoggerFactory.getLogger(MethodScanner.class);

    private final Raml raml;
    private final Method method;

    private Resource resource;
    private Action action;

    public MethodScanner(Raml raml, Method method) {
        this.raml = raml;
        this.method = method;
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
        for (AnnotationWrapper annotation : method.getAnnotationWrappers())
            if (annotation.getAnnotationType().getAnnotation(HttpMethod.class) != null)
                return ActionType.valueOf(annotation.getAnnotationType().getAnnotation(HttpMethod.class).value());
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
        ResourcePath path = ResourcePath.of(method.getContainerType());
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
        return camelCaseToWords(method.getName());
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
            Response response = new Response();

            response.setDescription(responseScanner.description());
            scanHeaders(responseScanner.responseHeaders(), response.getHeaders());
            scanBody(responseScanner.responseType(), response);

            action.getResponses().put(responseScanner.statusCodeString(), response);
        }
    }

    private void scanHeaders(List<ResponseHeaderScanner> scanners, Map<String, Header> map) {
        for (ResponseHeaderScanner headerScanner : scanners) {
            Header header = new Header();
            header.setDisplayName(headerScanner.name());
            header.setDescription(headerScanner.description());
            applyResponse(headerScanner, header);
            map.put(headerScanner.name(), header);
        }
    }

    private void applyResponse(ResponseHeaderScanner scanner, Header header) {
        Type response = scanner.response();
        if (response != null)
            new TypeInfo(response).applyTo(header);
    }

    private void scanBody(Type responseType, Response response) {
        new TypeInfo(responseType).applyTo(bodyMap(response), produces());
    }

    private Map<String, MimeType> bodyMap(Response response) {
        Map<String, MimeType> bodyMap = response.getBody();
        if (bodyMap == null) {
            bodyMap = new HashMap<>();
            response.setBody(bodyMap);
        }
        return bodyMap;
    }

    private List<String> produces() {
        if (method.isAnnotated(Produces.class))
            return asList(method.getAnnotation(Produces.class).value());
        if (raml.getMediaType() != null)
            return singletonList(raml.getMediaType());
        return singletonList(APPLICATION_JSON);
    }
}
