package com.github.t1.ramlap;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;

import javax.ws.rs.core.Response.*;

import com.github.t1.exap.reflection.*;

// TODO this should be merged into the com.github.t1.exap.reflection.TypeVisitor
public class FieldVisitor {
    protected void visit(Type type) {
        try {
            if (type.isBoolean()) {
                visitBoolean(type);
            } else if (type.isInteger() || isIntegerWrapper(type)) {
                visitInteger(type);
            } else if (type.isFloating()) {
                visitFloating(type);
            } else if (type.isString() || isStringWrapper(type) || isJacksonToString(type)) {
                visitString(type);
            } else if (type.isEnum()) {
                visitEnum(type);
            } else if (type.isArray()) {
                visitArray(type);
            } else if (type.isA(Collection.class)) {
                visitCollection(type);
            } else {
                visitObject(type);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("while visiting " + type, e);
        } catch (Error e) {
            throw new Error("while visiting " + type, e);
        }
    }

    protected void visitBoolean(Type type) {
        visitScalar(type);
    }

    protected void visitInteger(Type type) {
        visitScalar(type);
    }

    protected void visitFloating(Type type) {
        visitScalar(type);
    }

    protected void visitString(Type type) {
        visitScalar(type);
    }

    protected void visitEnum(Type type) {
        visitScalar(type);
    }

    protected void visitScalar(@SuppressWarnings("unused") Type type) {}

    protected void visitArray(Type type) {
        visitSequence(type.elementType());
    }

    protected void visitCollection(Type type) {
        visitSequence(type.getTypeParameters().get(0));
    }

    protected void visitSequence(Type type) {
        visit(type);
    }

    protected void visitObject(Type type) {
        for (Field field : type.getAllFields()) {
            if (field.isStatic() || field.isTransient())
                continue;
            visitField(field);
        }
    }

    protected void visitField(Field field) {
        visit(field.getType());
    }

    private boolean isJacksonToString(Type type) {
        return isUsing(type, org.codehaus.jackson.map.annotate.JsonSerialize.class,
                org.codehaus.jackson.map.ser.std.ToStringSerializer.class)
                || isUsing(type, com.fasterxml.jackson.databind.annotation.JsonSerialize.class,
                        com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class);
    }

    private boolean isUsing(Type type, Class<? extends Annotation> annotation, Class<?> serializer) {
        return type.isAnnotated(annotation) //
                && serializer.getName()
                        .contentEquals(type.getAnnotationWrapper(annotation).getTypeProperty("using").getFullName());
    }

    private boolean isIntegerWrapper(Type type) {
        return type.isA(StatusType.class);
    }

    private boolean isStringWrapper(Type type) {
        if (type.isA(Path.class) || type.isA(URI.class) || type.isA(Status.class) || type.isA(StatusType.class))
            return true;
        return hasToString(type) && hasFromString(type);
    }

    private boolean hasToString(Type type) {
        for (Method method : type.getAllMethods())
            if ("toString".equals(method.getName()) //
                    && method.getParameters().isEmpty() //
                    && !method.getDeclaringType().getFullName().equals(Object.class.getName()))
                return true;
        return false;
    }

    private boolean hasFromString(Type type) {
        for (Method method : type.getStaticMethods())
            if ("fromString".equals(method.getName()) //
                    && method.getParameters().size() == 1 && method.getParameter(0).getType().isString() //
                    && method.isPublic() && method.isStatic())
                return true;
        return false;
    }
}
