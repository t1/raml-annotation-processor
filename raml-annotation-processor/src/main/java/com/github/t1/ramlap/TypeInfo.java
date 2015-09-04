package com.github.t1.ramlap;

import static java.util.Locale.*;
import static java.util.Objects.*;
import static javax.ws.rs.core.MediaType.*;
import static org.raml.model.ParamType.*;

import java.io.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.raml.model.ParamType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.t1.exap.reflection.*;

public class TypeInfo {
    private final Type type;

    public TypeInfo(ProcessingEnvironment env, Class<?> type) {
        this(new ReflectionType(env, type));
    }

    public TypeInfo(Type type) {
        this.type = requireNonNull(type);
    }

    public boolean isSimple() {
        return type.isBoolean() || type.isNumber() || type.isString();
    }

    public ParamType paramType() {
        if (type.isBoolean())
            return BOOLEAN;
        if (type.isDecimal())
            return NUMBER;
        if (type.isInteger())
            return INTEGER;
        return STRING;
    }

    public String type() {
        return paramType().name().toLowerCase(US);
    }

    public String schema(String mediaType) {
        if (mediaType.equals(APPLICATION_JSON))
            return jsonSchema();
        if (mediaType.equals(APPLICATION_XML))
            return xmlSchema();
        return null;
    }

    public String jsonSchema() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(mapper.constructType(javaType()), visitor);
            JsonSchema schema = visitor.finalSchema();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        } catch (JsonProcessingException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String xmlSchema() {
        final StringWriter string = new StringWriter();

        try {
            JAXBContext context = JAXBContext.newInstance(javaType());
            context.generateSchema(new SchemaOutputResolver() {
                @Override
                public Result createOutput(String namespaceUri, String suggestedFileName) {
                    StreamResult result = new StreamResult(string);
                    result.setSystemId("dummy");
                    return result;
                }
            });
        } catch (ClassNotFoundException | JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
        return (string.toString().isEmpty()) ? null : string.toString();
    }

    private Class<?> javaType() throws ClassNotFoundException {
        return Class.forName(type.getQualifiedName());
    }

    @Override
    public String toString() {
        return "TypeInfo:" + type;
    }
}
