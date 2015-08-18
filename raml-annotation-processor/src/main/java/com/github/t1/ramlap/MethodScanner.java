package com.github.t1.ramlap;

import static com.github.t1.ramlap.RamlScanner.*;
import static com.github.t1.ramlap.StringTools.*;

import java.util.*;
import java.util.regex.Matcher;

import javax.ws.rs.*;

import org.raml.model.*;
import org.slf4j.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.ApiOperation;

public class MethodScanner {
    private static final Logger log = LoggerFactory.getLogger(MethodScanner.class);

    private final Method method;
    private final Raml raml;

    public MethodScanner(Raml raml, Method method) {
        this.raml = raml;
        this.method = method;
    }

    public void scan() {
        ActionType actionType = actionType();
        if (actionType == null)
            return;
        log.debug("scan {} method {}", actionType, method);
        Resource resource = resource();
        if (resource.getAction(actionType) != null) {
            method.warning("path not unique");
            return;
        }
        Action action = new Action();
        action.setResource(resource);
        action.setDisplayName(displayName());
        action.setDescription(description());
        action.setType(actionType);

        scanParams(resource, action);

        resource.getActions().put(actionType, action);
    }

    private void scanParams(Resource resource, Action action) {
        List<String> expectedPathParamNames = uriParams();
        for (Parameter parameter : method.getParameters()) {
            ParameterScanner scanner = new ParameterScanner(action, parameter);
            scanner.scan();
            String pathParameterId = scanner.getPathParameterId();
            if (pathParameterId != null && !expectedPathParamNames.remove(pathParameterId))
                parameter.warning("annotated path param name '" + pathParameterId + "' not defined in path '"
                        + resource.getUri() + "'");

        }
        for (String pathParameterName : expectedPathParamNames)
            method.warning("no path param annotated as '" + pathParameterName + "' found, but required in path '"
                    + resource.getUri() + "'");
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
        Path methodPath = method.getAnnotation(Path.class);
        if (methodPath == null)
            return ResourcePath.of(method.getType());
        return ResourcePath.of(method.getType()).and(methodPath.value());
    }

    private String displayName() {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null && !apiOperation.value().isEmpty())
            return apiOperation.value();
        JavaDoc javaDoc = method.getAnnotation(JavaDoc.class);
        if (javaDoc != null)
            return javaDoc.summary();
        return camelCaseToWords(method.getSimpleName());
    }

    private String description() {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null && !apiOperation.notes().isEmpty())
            return apiOperation.notes();
        JavaDoc javaDoc = method.getAnnotation(JavaDoc.class);
        return javaDoc == null ? null : javaDoc.value();
    }

    private List<String> uriParams() {
        List<String> list = new ArrayList<>();
        Matcher matcher = VARS.matcher(resource().getUri());
        while (matcher.find())
            list.add(matcher.group(1));
        return list;
    }
}
