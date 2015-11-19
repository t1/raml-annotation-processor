package com.github.t1.ramlap;

import static com.github.t1.ramlap.ProblemDetail.*;
import static java.util.Locale.*;
import static java.util.Objects.*;
import static org.raml.model.ParamType.*;

import java.util.*;
import java.util.regex.*;

import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.slf4j.*;

import com.github.t1.exap.reflection.Type;

public class TypeInfo {
    private static final Logger log = LoggerFactory.getLogger(TypeInfo.class);

    static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("(?<type>[^/]*)/(?<subtype>[^;]*)(;(?<params>.*))?");

    private final Type type;

    public TypeInfo(Type type) {
        this.type = requireNonNull(type);
    }

    public void applyTo(AbstractParam param) {
        param.setType(paramType());
        param.setEnumeration(type.getEnumValues());
    }

    public void applyTo(Map<String, MimeType> bodyMap, List<String> mediaTypes) {
        if (type.isVoid())
            return;
        if (isProblemDetail())
            mediaTypes = asProblemTypes(mediaTypes);
        for (String mediaType : mediaTypes) {
            MimeType mimeType = new MimeType();
            applyTo(mimeType, mediaType);
            bodyMap.put(mediaType.toString(), mimeType);
        }
    }

    private boolean isProblemDetail() {
        return type.isA(ProblemDetail.class);
    }

    private List<String> asProblemTypes(List<String> mediaTypes) {
        List<String> problemTypes = new ArrayList<>();
        for (String mediaType : mediaTypes)
            problemTypes.add(asProblemType(mediaType));
        return problemTypes;
    }

    private String asProblemType(String mediaType) {
        Matcher matcher = MEDIA_TYPE_PATTERN.matcher(mediaType);
        if (matcher.matches())
            return APPLICATION_PROBLEM_TYPE_PREFIX + "+" + matcher.group("subtype");
        return APPLICATION_PROBLEM_JSON;
    }

    private void applyTo(MimeType mimeType, String mediaType) {
        if (isSimple())
            mimeType.setType(paramType().name().toLowerCase(US));
        // TODO add schema to raml root and link from here
        mimeType.setSchema(schema(mediaType));
        mimeType.setExample(example(mediaType));
    }

    private String schema(String mediaType) {
        return isSimple() || isUnspecific() ? null : SchemaGenerator.schema(type, mediaType);
    }

    private boolean isUnspecific() {
        return javax.ws.rs.core.Response.class.getName().equals(type.getFullName());
    }

    private ParamType paramType() {
        if (type.isBoolean())
            return BOOLEAN;
        if (type.isFloating())
            return NUMBER;
        if (type.isInteger())
            return INTEGER;
        return STRING;
    }

    private boolean isSimple() {
        return type.isBoolean() || type.isNumber() || type.isString();
    }

    private String example(String mediaType) {
        if (isSimple() || isUnspecific())
            return null;
        try {
            return ExampleGenerator.example(type, mediaType);
        } catch (RuntimeException e) {
            String message = "failed to generate example for " + type + ": " + e.getMessage();
            if (RamlAnnotationProcessor.isStrict()) {
                log.error(message, e);
                type.error(message);
            } else {
                log.warn(message, e);
                type.warning(message);
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "TypeInfo:" + type.getFullName();
    }
}
