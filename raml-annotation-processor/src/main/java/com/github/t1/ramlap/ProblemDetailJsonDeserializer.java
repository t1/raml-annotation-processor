package com.github.t1.ramlap;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

import javax.ws.rs.core.Response.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ProblemDetailJsonDeserializer extends StdDeserializer<ProblemDetail> {
    private static final long serialVersionUID = 1L;

    // TODO handle unknown Status
    public static StatusType toStatus(String value) {
        return Status.valueOf(value);
    }

    protected ProblemDetailJsonDeserializer() {
        super(ProblemDetail.class);
    }

    private static class ProblemDetailParser {
        private final JsonNode tree;
        private ProblemDetail problem;

        public ProblemDetailParser(JsonNode tree) {
            this.tree = tree;
        }

        public ProblemDetail parse() {
            this.problem = ProblemDetail.of(asUri(tree.get("type")));
            set("title", t -> problem.title(t));
            set("status", t -> problem.status(toStatus(t)));
            set("detail", t -> problem.detail(t));
            set("instance", t -> problem.instance(URI.create(t)));
            return problem;
        }

        private URI asUri(JsonNode typeNode) {
            return (typeNode == null) ? null : URI.create(typeNode.asText());
        }

        private void set(String fieldName, Function<String, ProblemDetail> consumer) {
            if (tree.has(fieldName))
                problem = consumer.apply(tree.get(fieldName).asText());
        }
    }

    @Override
    public ProblemDetail deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        JsonNode tree = parser.getCodec().readTree(parser);
        return new ProblemDetailParser(tree).parse();
    }
}
