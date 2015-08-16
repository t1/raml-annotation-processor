package com.github.t1.ramlap;

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
        Action action = new Action();
        action.setResource(resource);
        action.setDisplayName(displayName());
        action.setDescription(description());
        action.setType(actionType);
        resource.getActions().put(actionType, action);
    }

    public ActionType actionType() {
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
            return typePath();
        return typePath().and(methodPath.value());
    }

    private ResourcePath typePath() {
        Type type = method.getType();
        Path path = type.getAnnotation(Path.class);
        if (path == null) {
            type.warning("missing annotation: " + Path.class.getName());
            return null;
        }
        return ResourcePath.of(path.value());
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

    private String camelCaseToWords(String string) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isUpperCase(c))
                out.append(' ');
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    private String description() {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null && !apiOperation.notes().isEmpty())
            return apiOperation.notes();
        JavaDoc javaDoc = method.getAnnotation(JavaDoc.class);
        return javaDoc == null ? null : javaDoc.value();
    }
}
