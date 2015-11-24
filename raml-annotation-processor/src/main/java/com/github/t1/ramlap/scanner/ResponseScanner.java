package com.github.t1.ramlap.scanner;

import static com.github.t1.ramlap.tools.StringTools.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.*;

import com.github.t1.exap.reflection.*;
import com.github.t1.ramlap.annotations.ApiResponse;
import com.github.t1.ramlap.scanner.ResponseHeaderScanner.*;
import com.github.t1.ramlap.tools.NonStandardStatus;

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
            for (AnnotationWrapper response : responses.getAnnotationProperties("value"))
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
        public List<ResponseHeaderScanner> responseHeaders() {
            List<ResponseHeaderScanner> list = new ArrayList<>();
            for (AnnotationWrapper header : annotationWrapper.getAnnotationProperties("responseHeaders"))
                if (!isDefaultResponseHeader(header))
                    list.add(headerScanner(header));
            return list;
        }

        private boolean isDefaultResponseHeader(AnnotationWrapper header) {
            return header.getStringProperty("name").isEmpty();
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
        public String description() {
            String title = annotationWrapper.getStringProperty("title");
            String detail = annotationWrapper.getStringProperty("detail");
            if (!title.isEmpty())
                return concat(title, " ", detail);
            Type type = annotationWrapper.getTypeProperty("type");
            if (type.isVoid())
                return detail.isEmpty() ? null : detail;
            title = camelCaseToWords(type.getSimpleName());
            return concat(title, ": ", detail);
        }

        private String concat(String title, String delimiter, String detail) {
            return detail.isEmpty() ? title : title + delimiter + detail;
        }

        @Override
        public StatusType status() {
            int code = annotationWrapper.getIntProperty("statusCode");
            if (code > 0) {
                StatusType statusFromCode = Status.fromStatusCode(code);
                if (statusFromCode == null)
                    return new NonStandardStatus(code);
                Status statusFromAnnotation = getStatus();
                if (statusFromAnnotation != ApiResponse.DEFAULT_STATUS && !statusFromAnnotation.equals(statusFromCode))
                    annotationWrapper.error("Conflicting specification of status " + statusFromAnnotation
                            + " and status code " + code + ". You should just use the status.");
                else
                    annotationWrapper.warning("Status code " + code + " is defined as '" + statusFromCode + "'. "
                            + "You should use the constant instead.");
                return statusFromCode;
            }
            return getStatus();
        }

        private Status getStatus() {
            String value = annotationWrapper.getEnumProperty("status");
            if (ApiResponse.DEFAULT_STATUS.name().equals(value)) {
                Type type = annotationWrapper.getTypeProperty("type");
                if (type != null) {
                    AnnotationWrapper referenced = type.getAnnotationWrapper(ApiResponse.class);
                    if (referenced != null) {
                        value = referenced.getEnumProperty("status");
                    }
                }
            }
            return Status.valueOf(value);
        }

        @Override
        protected Type type() {
            return annotationWrapper.getTypeProperty("type");
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
        public String description() {
            return annotationWrapper.getStringProperty("message");
        }

        @Override
        public StatusType status() {
            return Status.fromStatusCode(annotationWrapper.getIntProperty("code"));
        }

        @Override
        protected Type type() {
            return annotationWrapper.getTypeProperty("response");
        }

        @Override
        protected ResponseHeaderScanner headerScanner(AnnotationWrapper header) {
            return new SwaggerResponseHeaderScanner(header);
        }
    }

    public abstract StatusType status();

    public String statusCodeString() {
        return Integer.toString(status().getStatusCode());
    }

    public abstract String description();

    public abstract List<ResponseHeaderScanner> responseHeaders();

    public abstract Type responseType();
}
