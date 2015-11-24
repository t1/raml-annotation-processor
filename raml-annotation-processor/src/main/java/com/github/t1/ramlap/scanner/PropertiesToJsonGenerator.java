package com.github.t1.ramlap.scanner;

import java.io.StringWriter;
import java.util.*;

import javax.json.Json;
import javax.json.stream.*;

import org.slf4j.Logger;

import com.github.t1.exap.LoggingJsonGenerator;
import com.github.t1.exap.reflection.Type;

public abstract class PropertiesToJsonGenerator extends FieldVisitor {
    private final Logger log;
    protected JsonGenerator json;

    public PropertiesToJsonGenerator(Logger log) {
        this.log = log;
    }

    public String generate(Type type) {
        StringWriter out = new StringWriter();
        try (JsonGenerator json = createJsonGenerator(out)) {
            this.json = json;
            writeStart(type);
            visit(type);
            writeEnd();
        }
        return out.toString().trim() + "\n";
    }

    protected void writeStart(@SuppressWarnings("unused") Type type) {}

    protected void writeEnd() {}

    @SuppressWarnings("resource") // the caller closes
    private JsonGenerator createJsonGenerator(StringWriter out) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
        JsonGenerator generator = factory.createGenerator(out);
        if (log.isTraceEnabled())
            generator = LoggingJsonGenerator.of(log, generator);
        return generator;
    }
}
