package com.github.t1.ramlap;

import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import java.nio.file.AccessMode;
import java.util.*;

import org.junit.*;

import com.github.t1.exap.reflection.Type;

public class SchemaGeneratorJsonTest {
    private static final String SCHEMA = "    \"$schema\":\"http://json-schema.org/schema#\",\n";

    @Test
    public void shouldGenerateString() {
        String json = SchemaGenerator.schema(Type.of(String.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateEnum() {
        String json = SchemaGenerator.schema(Type.of(AccessMode.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
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
        String json = SchemaGenerator.schema(Type.of(Integer.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"integer\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateDouble() {
        String json = SchemaGenerator.schema(Type.of(Double.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"number\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateBoolean() {
        String json = SchemaGenerator.schema(Type.of(boolean.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"boolean\"\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateStringArray() {
        String json = SchemaGenerator.schema(Type.of(String[].class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"array\",\n" //
                + "    \"items\":{\n" //
                + "        \"type\":\"string\"\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @Test
    public void shouldGenerateIntegerArray() {
        String json = SchemaGenerator.schema(Type.of(Integer[].class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"array\",\n" //
                + "    \"items\":{\n" //
                + "        \"type\":\"integer\"\n" //
                + "    }\n" //
                + "}\n", json);
    }

    static class Pojo {
        String value;
        Long integer;
        boolean bool;
    }

    @Test
    public void shouldGeneratePojo() {
        String json = SchemaGenerator.schema(Type.of(Pojo.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$Pojo\",\n" //
                + "    \"properties\":{\n" //
                + "        \"value\":{\n" //
                + "            \"type\":\"string\"\n" //
                + "        },\n" //
                + "        \"integer\":{\n" //
                + "            \"type\":\"integer\"\n" //
                + "        },\n" //
                + "        \"bool\":{\n" //
                + "            \"type\":\"boolean\"\n" //
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
        String json = SchemaGenerator.schema(Type.of(PojoWithList.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
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
                + "                },\n" //
                + "                \"bool\":{\n" //
                + "                    \"type\":\"boolean\"\n" //
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

    static class PojoWithSet {
        Set<Pojo> set;
    }

    @Test
    public void shouldGeneratePojoWithSet() {
        String json = SchemaGenerator.schema(Type.of(PojoWithSet.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$PojoWithSet\",\n" //
                + "    \"properties\":{\n" //
                + "        \"set\":{\n" //
                + "            \"type\":\"array\",\n" //
                + "            \"items\":{\n" //
                + "                \"type\":\"object\",\n" //
                + "                \"properties\":{\n" //
                + "                    \"value\":{\n" //
                + "                        \"type\":\"string\"\n" //
                + "                    },\n" //
                + "                    \"integer\":{\n" //
                + "                        \"type\":\"integer\"\n" //
                + "                    },\n" //
                + "                    \"bool\":{\n" //
                + "                        \"type\":\"boolean\"\n" //
                + "                    }\n" //
                + "                }\n" //
                + "            }\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    @org.codehaus.jackson.map.annotate.JsonSerialize(using = org.codehaus.jackson.map.ser.std.ToStringSerializer.class)
    static class CodehausToStringPojo {
        long value;
    }

    @Test
    public void shouldGenerateCodehausToStringPojo() {
        String json = SchemaGenerator.schema(Type.of(CodehausToStringPojo.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$CodehausToStringPojo\"\n" //
                + "}\n", json);
    }

    @com.fasterxml.jackson.databind.annotation.JsonSerialize(
            using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    static class FasterXmlToStringPojo {
        long value;
    }

    @Test
    public void shouldGenerateFasterXmlToStringPojo() {
        String json = SchemaGenerator.schema(Type.of(FasterXmlToStringPojo.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"string\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$FasterXmlToStringPojo\"\n" //
                + "}\n", json);
    }

    static class RecursivePojo {
        RecursivePojo pojo;
    }

    @Test
    @Ignore("requires $ref from json-schema draft-4")
    public void shouldGenerateRecursivePojo() {
        String json = SchemaGenerator.schema(Type.of(RecursivePojo.class), APPLICATION_JSON);

        assertEquals("" //
                + "{\n" //
                + SCHEMA //
                + "    \"type\":\"object\",\n" //
                + "    \"id\":\"urn:jsonschema:com:github:t1:ramlap:SchemaGeneratorJsonTest$RecursivePojo\",\n" //
                + "    \"properties\":{\n" //
                + "        \"pojo\":{\n" //
                + "            \"type\":\"object\",\n" //
                + "            \"properties\":{\n" //
                + "                \"value\":{\n" //
                + "                    \"type\":\"string\"\n" //
                + "                }\n" //
                + "            }\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n", json);
    }

    // TODO UUID
    // TODO Map
    // TODO Path
}
