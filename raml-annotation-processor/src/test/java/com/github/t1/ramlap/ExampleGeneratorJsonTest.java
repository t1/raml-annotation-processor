package com.github.t1.ramlap;

import static com.github.t1.exap.reflection.ReflectionProcessingEnvironment.*;
import static javax.ws.rs.core.MediaType.*;
import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.*;
import java.util.List;

import javax.ws.rs.core.Response.*;

import org.junit.*;

import com.github.t1.exap.reflection.Type;

import io.swagger.annotations.ApiModelProperty;

public class ExampleGeneratorJsonTest {
    static class ScalarsPojo {
        boolean b;
        int i;
        double d;
        String s;
        URI u;
        Path p;
        Status h;
        StatusType j;
        AccessMode m;
    }

    @Test
    public void shouldProduceScalarExamples() {
        Type type = ENV.type(ScalarsPojo.class);

        String example = ExampleGenerator.example(type, APPLICATION_JSON);

        assertThat(example).isEqualTo("{\n" //
                + "    \"b\":false,\n" //
                + "    \"i\":12345,\n" //
                + "    \"d\":123.45,\n" //
                + "    \"s\":\"foo\",\n" //
                + "    \"u\":\"http://example.org/foo\",\n" //
                + "    \"p\":\"/foo/bar\",\n" //
                + "    \"h\":400,\n" //
                + "    \"j\":400,\n" //
                + "    \"m\":\"READ\"\n" //
                + "}\n");
    }

    static class SequencesPojo {
        AccessMode[] a;
        List<String> c;
    }

    @Test
    public void shouldProduceSequencesExamples() {
        Type type = ENV.type(SequencesPojo.class);

        String example = ExampleGenerator.example(type, APPLICATION_JSON);

        assertThat(example).isEqualTo("{\n" //
                + "    \"a\":[\n" //
                + "        \"READ\"\n" //
                + "    ],\n" //
                + "    \"c\":[\n" //
                + "        \"foo\"\n" //
                + "    ]\n" //
                + "}\n");
    }

    static class SequencesOfSequencesPojo {
        SequencesPojo[] sa;
        List<SequencesPojo> sl;
    }

    @Test
    @Ignore("FIXME!")
    public void shouldProduceSequencesOfSequencesExamples() {
        Type type = ENV.type(SequencesOfSequencesPojo.class);

        String example = ExampleGenerator.example(type, APPLICATION_JSON);

        assertThat(example).isEqualTo("{\n" //
                + "    \"sa\":[\n" //
                + "        \"a\":[\n" //
                + "            \"READ\"\n" //
                + "        ],\n" //
                + "        \"c\":[\n" //
                + "            \"foo\"\n" //
                + "        ]\n" //
                + "    ],\n" //
                + "    \"l\":[\n" //
                + "        \"a\":[\n" //
                + "            \"READ\"\n" //
                + "        ],\n" //
                + "        \"c\":[\n" //
                + "            \"foo\"\n" //
                + "        ]\n" //
                + "    ]\n" //
                + "}\n");
    }

    static class NestedNestedPojo {
        String z;
    }

    static class NestedPojo {
        String x, y;
        NestedNestedPojo n;
    }

    static class NestingPojo {
        NestedPojo o;
    }

    @Test
    public void shouldProduceNestedExamples() {
        Type type = ENV.type(NestingPojo.class);

        String example = ExampleGenerator.example(type, APPLICATION_JSON);

        assertThat(example).isEqualTo("{\n" //
                + "    \"o\":{\n" //
                + "        \"x\":\"foo\",\n" //
                + "        \"y\":\"foo\",\n" //
                + "        \"n\":{\n" //
                + "            \"z\":\"foo\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n");
    }

    static class AnnotatedNestedNestedPojo {
        @ApiModelProperty(example = "xz")
        String z;
    }

    static class AnnotatedNestedPojo {
        @ApiModelProperty(example = "xx")
        String x;

        @ApiModelProperty(example = "xy")
        String y;

        AnnotatedNestedNestedPojo n;
    }

    static class ApiModelPropertyAnnotatedExamplesPojo {
        @ApiModelProperty(example = "true")
        boolean b;

        @ApiModelProperty(example = "9876")
        int i;

        @ApiModelProperty(example = "98.76")
        double d;

        @ApiModelProperty(example = "xs")
        String s;

        @ApiModelProperty(example = "xu")
        URI u;

        @ApiModelProperty(example = "xp")
        Path p;

        @ApiModelProperty(example = "403")
        Status h;

        @ApiModelProperty(example = "403")
        StatusType j;

        @ApiModelProperty(example = "xm")
        AccessMode m;

        @ApiModelProperty(example = "xa")
        AccessMode[] a;

        @ApiModelProperty(example = "xc")
        List<String> c;

        AnnotatedNestedPojo o;
    }

    @Test
    public void shouldProduceApiModelPropertyAnnotatedExamples() {
        Type type = ENV.type(ApiModelPropertyAnnotatedExamplesPojo.class);

        String example = ExampleGenerator.example(type, APPLICATION_JSON);

        assertThat(example).isEqualTo("{\n" //
                + "    \"b\":true,\n" //
                + "    \"i\":9876,\n" //
                + "    \"d\":98.76,\n" //
                + "    \"s\":\"xs\",\n" //
                + "    \"u\":\"xu\",\n" //
                + "    \"p\":\"xp\",\n" //
                + "    \"h\":403,\n" //
                + "    \"j\":403,\n" //
                + "    \"m\":\"xm\",\n" //
                + "    \"a\":[\n" //
                + "        \"xa\"\n" //
                + "    ],\n" //
                + "    \"c\":[\n" //
                + "        \"xc\"\n" //
                + "    ],\n" //
                + "    \"o\":{\n" //
                + "        \"x\":\"xx\",\n" //
                + "        \"y\":\"xy\",\n" //
                + "        \"n\":{\n" //
                + "            \"z\":\"xz\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n");
    }

    static class ApiExampleAnnotatedExamplesPojo {
        @ApiExample("true")
        boolean b;

        @ApiExample("9876")
        int i;

        @ApiExample("98.76")
        double d;

        @ApiExample("xs")
        String s;

        @ApiExample("xu")
        URI u;

        @ApiExample("xp")
        Path p;

        @ApiExample("403")
        Status h;

        @ApiExample("403")
        StatusType j;

        @ApiExample("xm")
        AccessMode m;

        @ApiExample("xa")
        AccessMode[] a;

        @ApiExample("xc")
        List<String> c;

        AnnotatedNestedPojo o;
    }

    @Test
    public void shouldProduceApiExampleAnnotatedExamples() {
        Type type = ENV.type(ApiExampleAnnotatedExamplesPojo.class);

        String example = ExampleGenerator.example(type, APPLICATION_JSON);

        assertThat(example).isEqualTo("{\n" //
                + "    \"b\":true,\n" //
                + "    \"i\":9876,\n" //
                + "    \"d\":98.76,\n" //
                + "    \"s\":\"xs\",\n" //
                + "    \"u\":\"xu\",\n" //
                + "    \"p\":\"xp\",\n" //
                + "    \"h\":403,\n" //
                + "    \"j\":403,\n" //
                + "    \"m\":\"xm\",\n" //
                + "    \"a\":[\n" //
                + "        \"xa\"\n" //
                + "    ],\n" //
                + "    \"c\":[\n" //
                + "        \"xc\"\n" //
                + "    ],\n" //
                + "    \"o\":{\n" //
                + "        \"x\":\"xx\",\n" //
                + "        \"y\":\"xy\",\n" //
                + "        \"n\":{\n" //
                + "            \"z\":\"xz\"\n" //
                + "        }\n" //
                + "    }\n" //
                + "}\n");
        // TODO RAML 1.0: example displayName, description, annotations, serialized examples, and strict
    }


    @Test
    public void shouldProduceStringArrayExample() {
        Type stringArray = ENV.type(String[].class);

        String example = ExampleGenerator.example(stringArray, APPLICATION_JSON);

        assertThat(example).isEqualTo("[\n" //
                + "    \"foo\"\n" //
                + "]\n");
    }

    @Test
    public void shouldProduceStringListExample() {
        class Dummy {
            @SuppressWarnings("unused")
            List<String> list;
        }
        Type stringList = ENV.type(Dummy.class).getField("list").getType();

        String example = ExampleGenerator.example(stringList, APPLICATION_JSON);

        assertThat(example).isEqualTo("[\n" //
                + "    \"foo\"\n" //
                + "]\n");
    }

    @Test
    public void shouldProducePojoArrayExample() {
        Type pojoArray = ENV.type(Pojo[].class);

        String example = ExampleGenerator.example(pojoArray, APPLICATION_JSON);

        assertThat(example).isEqualTo("[\n" //
                + "    {\n" //
                + "        \"value\":\"example-value\"\n" //
                + "    }\n" //
                + "]\n");
    }

    @Test
    public void shouldProducePojoListExample() {
        class Dummy {
            @SuppressWarnings("unused")
            List<Pojo> list;
        }
        Type pojoList = ENV.type(Dummy.class).getField("list").getType();

        String example = ExampleGenerator.example(pojoList, APPLICATION_JSON);

        assertThat(example).isEqualTo("[\n" //
                + "    {\n" //
                + "        \"value\":\"example-value\"\n" //
                + "    }\n" //
                + "]\n");
    }
}
