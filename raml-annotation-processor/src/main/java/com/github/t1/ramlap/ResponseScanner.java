package com.github.t1.ramlap;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.*;

import com.github.t1.exap.reflection.*;
import com.github.t1.ramlap.ResponseHeaderScanner.*;

public abstract class ResponseScanner {
    public static List<ResponseScanner> responses(Method method) {
        if (method.isAnnotated(ApiResponse.class))
            return ramlResponses(method);
        if (method.isAnnotated(io.swagger.annotations.ApiResponses.class))
            return swaggerResponses(method);
        if (method.isAnnotated(io.swagger.annotations.ApiResponse.class)) // not yet @Repeatable
            return asList(new SwaggerApiResponseScanner(method,
                    method.getAnnotationWrapper(io.swagger.annotations.ApiResponse.class)));
        return asList(new DefaultResponseScanner(method));
    }

    private static List<ResponseScanner> ramlResponses(Method method) {
        List<ResponseScanner> list = new ArrayList<>();
        for (AnnotationWrapper response : method.getAnnotationWrappers(ApiResponse.class))
            list.add(new ApiResponseScanner(method, response));
        return list;
    }

    private static List<ResponseScanner> swaggerResponses(Method method) {
        List<ResponseScanner> list = new ArrayList<>();
        for (AnnotationWrapper responses : method.getAnnotationWrappers(io.swagger.annotations.ApiResponses.class))
            for (AnnotationWrapper response : responses.getAnnotationsValue("value"))
                list.add(new SwaggerApiResponseScanner(method, response));
        return list;
    }

    private static class DefaultResponseScanner extends ResponseScanner {
        private final Method method;

        public DefaultResponseScanner(Method method) {
            this.method = method;
        }

        @Override
        public StatusType status() {
            return OK;
        }

        @Override
        public String description() {
            return "Success";
        }

        @Override
        public List<ResponseHeaderScanner> responseHeaders() {
            return emptyList();
        }

        @Override
        public Type responseType() {
            return method.getReturnType();
        }
    }

    private static abstract class AnnotatedResponseScanner extends ResponseScanner {
        private final Method method;
        protected final AnnotationWrapper annotationWrapper;

        public AnnotatedResponseScanner(Method method, AnnotationWrapper annotationWrapper) {
            this.method = method;
            this.annotationWrapper = annotationWrapper;
        }

        @Override
        public abstract StatusType status();

        @Override
        public String description() {
            return annotationWrapper.getStringValue("title");
        }

        @Override
        public List<ResponseHeaderScanner> responseHeaders() {
            List<ResponseHeaderScanner> list = new ArrayList<>();
            for (AnnotationWrapper header : annotationWrapper.getAnnotationsValue("responseHeaders"))
                if (!isDefaultResponseHeader(header))
                    list.add(headerScanner(header));
            return list;
        }

        private boolean isDefaultResponseHeader(AnnotationWrapper header) {
            return header.getStringValue("name").isEmpty();
        }

        protected abstract ResponseHeaderScanner headerScanner(AnnotationWrapper header);

        @Override
        public Type responseType() {
            Type type = type();
            if (type == null)
                return null;
            if (!type.isVoid())
                return type;
            return method.getReturnType();
        }

        protected abstract Type type();
    }

    private static class ApiResponseScanner extends AnnotatedResponseScanner {
        public ApiResponseScanner(Method method, AnnotationWrapper annotationWrapper) {
            super(method, annotationWrapper);
        }

        @Override
        public StatusType status() {
            int code = annotationWrapper.getIntValue("statusCode");
            if (code > 0) {
                StatusType statusFromCode = Status.fromStatusCode(code);
                if (statusFromCode == null)
                    return new NonStandardStatus(code);
                Status statusFromAnnotation = (Status) annotationWrapper.getValue("status");
                if (statusFromAnnotation != ApiResponse.DEFAULT_STATUS && !statusFromAnnotation.equals(statusFromCode))
                    annotationWrapper.error("Conflicting specification of status " + statusFromAnnotation
                            + " and status code " + code + ". You should just use the status.");
                else
                    annotationWrapper.warning("Status code " + code + " is defined as " + statusFromCode
                            + ". You should use that instead.");
                return statusFromCode;
            }
            return (Status) annotationWrapper.getValue("status");
        }

        @Override
        protected Type type() {
            return annotationWrapper.getTypeValue("type");
        }

        @Override
        protected ResponseHeaderScanner headerScanner(AnnotationWrapper header) {
            return new ApiResponseHeaderScanner(header);
        }
    }

    private static class SwaggerApiResponseScanner extends AnnotatedResponseScanner {
        public SwaggerApiResponseScanner(Method method, AnnotationWrapper annotationWrapper) {
            super(method, annotationWrapper);
        }

        @Override
        public StatusType status() {
            return Status.fromStatusCode(annotationWrapper.getIntValue("code"));
        }

        @Override
        protected Type type() {
            return annotationWrapper.getTypeValue("response");
        }

        @Override
        protected ResponseHeaderScanner headerScanner(AnnotationWrapper header) {
            return new SwaggerResponseHeaderScanner(header);
        }
    }

    public abstract StatusType status();

    public abstract String description();

    public abstract List<ResponseHeaderScanner> responseHeaders();

    public abstract Type responseType();
}
