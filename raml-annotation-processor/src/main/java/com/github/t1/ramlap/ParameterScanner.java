package com.github.t1.ramlap;

import static org.raml.model.ParamType.*;

import javax.ws.rs.*;

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

    private void scanJavaDoc(AbstractParam model) {
        if (parameter.getAnnotation(JavaDoc.class) != null) {
            model.setDisplayName(parameter.getAnnotation(JavaDoc.class).summary());
            model.setDescription(parameter.getAnnotation(JavaDoc.class).value());
        }
    }
}
