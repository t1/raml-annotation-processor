package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

public class SchemaGenerator {
    private final String name;

    public SchemaGenerator(String name) {
        this.name = name;
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
        return Class.forName(name);
    }
}

