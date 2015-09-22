package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import java.nio.file.AccessMode;
import java.util.List;

import org.junit.Test;

import com.github.t1.exap.reflection.ReflectionType;

public class SchemaGeneratorJsonTest {
    @Test
    public void shouldGenerateString() {
        String json = SchemaGenerator.schema(new ReflectionType(null, String.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"string\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateEnum() {
        String json = SchemaGenerator.schema(new ReflectionType(null, AccessMode.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"string\",\n" //
                + "    \"enum\":[\n" //
                + "        \"READ\",\n" //
                + "        \"WRITE\",\n" //
                + "        \"EXECUTE\"\n" //
                + "    ]\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateInteger() {
        String json = SchemaGenerator.schema(new ReflectionType(null, Integer.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"integer\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateDouble() {
        String json = SchemaGenerator.schema(new ReflectionType(null, Double.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"number\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateBoolean() {
        String json = SchemaGenerator.schema(new ReflectionType(null, Boolean.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"boolean\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateStringArray() {
        String json = SchemaGenerator.schema(new ReflectionType(null, String[].class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"array\",\n" //
                + "    \"items\":{\n" //
                + "        \"type\":\"string\"\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateIntegerArray() {
        String json = SchemaGenerator.schema(new ReflectionType(null, Integer[].class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"array\",\n" //
                + "    \"items\":{\n" //
                + "        \"type\":\"integer\"\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class Pojo {
        String value;
        Long integer;
    }

    @Test
    public void shouldGeneratePojo() {
        String json = SchemaGenerator.schema(new ReflectionType(null, Pojo.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$Pojo\",\n" //
                + "    \"properties\":{\n" //
                + "        \"value\":{\n" //
                + "            \"type\":\"string\"\n" //
                + "        },\n" //
                + "        \"integer\":{\n" //
                + "            \"type\":\"integer\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class PojoWithList {
        Pojo pojo;
        List<String> list;
    }

    @Test
    public void shouldGeneratePojoWithList() {
        String json = SchemaGenerator.schema(new ReflectionType(null, PojoWithList.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithList\",\n" //
                + "    \"properties\":{\n" //
                + "        \"pojo\":{\n" //
                + "            \"type\":\"object\",\n" //
                + "            \"properties\":{\n" //
                + "                \"value\":{\n" //
                + "                    \"type\":\"string\"\n" //
                + "                },\n" //
                + "                \"integer\":{\n" //
                + "                    \"type\":\"integer\"\n" //
                + "                }\n" //
                + "            }\n" //
                + "        },\n" //
                + "        \"list\":{\n" //
                + "            \"type\":\"array\",\n" //
                + "            \"items\":{\n" //
                + "                \"type\":\"string\"\n" //
                + "            }\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }
}
