package com.github.t1.ramlap;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;

import com.github.t1.exap.reflection.*;

public class PropertyVisitor {
    protected void visit(Type type) {
        try {
            if (type.isBoolean()) {
                visitBoolean();
            } else if (type.isInteger()) {
                visitInteger();
            } else if (type.isFloating()) {
                visitFloating();
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

    protected void visitBoolean() {}

    protected void visitInteger() {}

    protected void visitFloating() {}

    protected void visitString(@SuppressWarnings("unused") Type type) {}

    protected void visitEnum(@SuppressWarnings("unused") Type type) {}

    protected void visitArray(@SuppressWarnings("unused") Type type) {}

    protected void visitCollection(@SuppressWarnings("unused") Type type) {}

    protected void visitObject(Type type) {
        for (Field field : type.getAllFields()) {
            if (field.isStatic() || field.isTransient())
                continue;
            visitField(field);
        }
    }

    protected void visitField(@SuppressWarnings("unused") Field field) {}

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

    private boolean isStringWrapper(Type type) {
        if (type.isA(Path.class) || type.isA(URI.class))
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
