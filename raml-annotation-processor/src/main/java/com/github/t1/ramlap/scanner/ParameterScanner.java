package com.github.t1.ramlap.scanner;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.Parameter;
import com.github.t1.ramlap.tools.*;
import io.swagger.annotations.*;
import org.raml.model.*;
import org.raml.model.parameter.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;

public class ParameterScanner {
    private final Raml raml;
    private final Action action;
    private final Parameter parameter;
    private int paramAnnotationCount = 0;

    public ParameterScanner(Raml raml, Action action, Parameter parameter) {
        this.raml = raml;
        this.action = action;
        this.parameter = parameter;
    }

    public void scan() {
        scan(parameter.getAnnotation(PathParam.class));
        scan(parameter.getAnnotation(QueryParam.class));
        scan(parameter.getAnnotation(HeaderParam.class));

        scanBody();

        if (paramAnnotationCount > 1)
            parameter.warning("method parameters can be only be annotated as one of "
                    + "path, query, header, cookie, bean, form, or matrix parameter");
    }

    private void scan(PathParam pathParam) {
        if (pathParam == null)
            return;
        paramAnnotationCount++;
        UriParameter uriParam = uriParameter(pathParam.value());
        typeInfo().applyTo(uriParam);
        scanDoc(uriParam);
    }

    private UriParameter uriParameter(String uriParamName) {
        String uri = action.getResource().getUri();
        Optional<ResourcePathVariable> var = ResourcePath.of(uri).var(uriParamName);
        Resource resource;
        if (!var.isPresent()) {
            parameter.warning("annotated path param name '" + uriParamName + "' "
                    + "not defined in " + action.getType() + " of '" + uri + "'");
            resource = action.getResource();
        } else {
            resource = raml.getResource(var.get().getResourcePath().toString());
        }
        UriParameter model = resource.getResolvedUriParameters().get(uriParamName);
        if (model == null) {
            model = new UriParameter(uriParamName);
            resource.getUriParameters().put(uriParamName, model);
        } else {
            // TODO check that the existing and the new param match
        }
        return model;
    }


    private void scan(QueryParam queryParamAnnotation) {
        if (queryParamAnnotation == null)
            return;
        paramAnnotationCount++;
        String parameterId = queryParamAnnotation.value();
        QueryParameter queryParamModel = action.getQueryParameters().get(parameterId);
        if (queryParamModel == null) {
            queryParamModel = new QueryParameter();
            queryParamModel.setDisplayName(parameterId);
            action.getQueryParameters().put(parameterId, queryParamModel);
        }
        typeInfo().applyTo(queryParamModel);
        scanDoc(queryParamModel);
    }

    private void scan(HeaderParam headerParamAnnotation) {
        if (headerParamAnnotation == null)
            return;
        paramAnnotationCount++;
        String parameterId = headerParamAnnotation.value();
        Header headerParamModel = action.getHeaders().get(parameterId);
        if (headerParamModel == null) {
            headerParamModel = new Header();
            headerParamModel.setDisplayName(parameterId);
            action.getHeaders().put(parameterId, headerParamModel);
        }
        typeInfo().applyTo(headerParamModel);
        scanDoc(headerParamModel);
    }

    private void scanBody() {
        if (paramAnnotationCount > 0 || parameter.isAnnotated(Context.class))
            return;
        typeInfo().applyTo(bodyMap(), mediaTypes());
    }

    private Map<String, MimeType> bodyMap() {
        Map<String, MimeType> bodyMap = action.getBody();
        if (bodyMap == null) {
            bodyMap = new LinkedHashMap<>();
            action.setBody(bodyMap);
        }
        return bodyMap;
    }

    private TypeInfo typeInfo() {
        return new TypeInfo(parameter.getType());
    }

    private List<String> mediaTypes() {
        if (parameter.getMethod().isAnnotated(Consumes.class))
            return asList(parameter.getMethod().getAnnotation(Consumes.class).value());
        if (parameter.getMethod().getDeclaringType().isAnnotated(Consumes.class))
            return asList(parameter.getMethod().getDeclaringType().getAnnotation(Consumes.class).value());
        return singletonList(APPLICATION_JSON);
    }

    private void scanDoc(AbstractParam model) {
        if (parameter.isAnnotated(ApiParam.class)) {
            ApiParam apiParam = parameter.getAnnotation(ApiParam.class);
            model.setDescription(apiParam.value());
        } else if (parameter.getType().isAnnotated(ApiModel.class)) {
            ApiModel apiModel = parameter.getType().getAnnotation(ApiModel.class);
            if (!apiModel.value().isEmpty())
                model.setDisplayName(apiModel.value());
            if (!apiModel.description().isEmpty())
                model.setDescription(apiModel.description());
        } else if (parameter.isAnnotated(JavaDoc.class)) {
            JavaDoc javadoc = parameter.getAnnotation(JavaDoc.class);
            model.setDisplayName(JavaDoc.SUMMARY.apply(javadoc));
            model.setDescription(javadoc.value());
        }
    }
}
