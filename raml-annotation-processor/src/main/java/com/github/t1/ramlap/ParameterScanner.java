package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;
import static org.raml.model.ParamType.*;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import org.raml.model.*;
import org.raml.model.parameter.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.Parameter;

public class ParameterScanner {
    private final Action action;
    private final Parameter parameter;
    private String pathParameterId;
    private int paramAnnotationCount = 0;

    public ParameterScanner(Action action, Parameter parameter) {
        this.action = action;
        this.parameter = parameter;
    }

    public String getPathParameterId() {
        return pathParameterId;
    }

    public void scan() {
        scan(parameter.getAnnotation(PathParam.class));
        scan(parameter.getAnnotation(QueryParam.class));
        scan(parameter.getAnnotation(HeaderParam.class));

        scanBody();

        if (paramAnnotationCount > 1)
            parameter.warning("method parameters can be only be annotated as one of " //
                    + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    private void scan(PathParam pathParam) {
        if (pathParam == null)
            return;
        paramAnnotationCount++;
        pathParameterId = pathParam.value();
        Resource resource = action.getResource();
        UriParameter model = resource.getResolvedUriParameters().get(pathParameterId);
        if (model == null) {
            model = new UriParameter(pathParameterId);
            resource.getUriParameters().put(pathParameterId, model);
        }
        model.setType(type());
        scanJavaDoc(model);
    }

    private void scan(QueryParam queryParam) {
        if (queryParam == null)
            return;
        paramAnnotationCount++;
        String parameterId = queryParam.value();
        QueryParameter model = action.getQueryParameters().get(parameterId);
        if (model == null) {
            model = new QueryParameter();
            model.setDisplayName(parameterId);
            action.getQueryParameters().put(parameterId, model);
        }
        model.setType(type());
        scanJavaDoc(model);
    }

    private void scan(HeaderParam headerParam) {
        if (headerParam == null)
            return;
        paramAnnotationCount++;
        String parameterId = headerParam.value();
        Header model = action.getHeaders().get(parameterId);
        if (model == null) {
            model = new Header();
            model.setDisplayName(parameterId);
            action.getHeaders().put(parameterId, model);
        }
        model.setType(type());
        scanJavaDoc(model);
    }

    private void scanBody() {
        if (paramAnnotationCount > 0 || parameter.getAnnotation(Context.class) != null)
            return;
        Map<String, MimeType> bodyMap = action.getBody();
        if (bodyMap == null) {
            bodyMap = new LinkedHashMap<>();
            action.setBody(bodyMap);
        }
        for (String mediaType : mediaTypes()) {
            MimeType mimeType = new MimeType();
            mimeType.setType(mediaType);
            bodyMap.put(mediaType, mimeType);
        }
    }

    private String[] mediaTypes() {
        if (parameter.getMethod().getAnnotation(Consumes.class) != null)
            return parameter.getMethod().getAnnotation(Consumes.class).value();
        return new String[] { WILDCARD };
    }

    private ParamType type() {
        Class<?> primitiveType = parameter.getPrimitiveType();
        if (primitiveType == boolean.class)
            return BOOLEAN;
        if (primitiveType == double.class || primitiveType == float.class)
            return NUMBER;
        if (primitiveType == byte.class || primitiveType == short.class || primitiveType == int.class
                || primitiveType == long.class)
            return INTEGER;
        return STRING;
    }

    private void scanJavaDoc(AbstractParam model) {
        if (parameter.getAnnotation(JavaDoc.class) != null) {
            model.setDisplayName(parameter.getAnnotation(JavaDoc.class).summary());
            model.setDescription(parameter.getAnnotation(JavaDoc.class).value());
        }
    }
}
