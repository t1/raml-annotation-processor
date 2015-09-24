package com.github.t1.ramlap;

import static java.util.Locale.*;
import static java.util.Objects.*;
import static org.raml.model.ParamType.*;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;

import com.github.t1.exap.reflection.*;

public class TypeInfo {
    private final Type type;

    public TypeInfo(ProcessingEnvironment env, Class<?> type) {
        this(new ReflectionType(env, type));
    }

    public TypeInfo(Type type) {
        this.type = requireNonNull(type);
    }

    public void applyTo(AbstractParam param) {
        param.setType(paramType());
        param.setEnumeration(type.getEnumValues());
    }

    public void applyTo(Map<String, MimeType> bodyMap, String[] mediaTypes) {
        if (type.isVoid())
            return;
        for (String mediaType : mediaTypes) {
            MimeType mimeType = new MimeType();
            applyTo(mimeType, mediaType);
            bodyMap.put(mediaType, mimeType);
        }
    }

    private void applyTo(MimeType mimeType, String mediaType) {
        mimeType.setType(isSimple() ? paramType().name().toLowerCase(US) : null);
        mimeType.setSchema(isSimple() || isUnspecific() ? null : SchemaGenerator.schema(type, mediaType));
    }

    private boolean isUnspecific() {
        return javax.ws.rs.core.Response.class.getName().equals(type.getQualifiedName());
    }

    private ParamType paramType() {
        if (type.isBoolean())
            return BOOLEAN;
        if (type.isDecimal())
            return NUMBER;
        if (type.isInteger())
            return INTEGER;
        return STRING;
    }

    private boolean isSimple() {
        return type.isBoolean() || type.isNumber() || type.isString();
    }

    @Override
    public String toString() {
        return "TypeInfo:" + type.getQualifiedName();
    }
}
