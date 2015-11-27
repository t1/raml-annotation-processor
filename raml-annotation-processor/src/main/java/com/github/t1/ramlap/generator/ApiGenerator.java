package com.github.t1.ramlap.generator;

import static com.github.t1.exap.generator.TypeKind.*;
import static com.github.t1.ramlap.tools.StringTools.*;

import java.io.*;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.raml.model.*;
import org.raml.model.Resource;
import org.raml.parser.loader.*;
import org.raml.parser.visitor.YamlDocumentBuilder;

import com.github.t1.exap.generator.*;
import com.github.t1.exap.reflection.*;
import com.github.t1.exap.reflection.Package;
import com.github.t1.ramlap.RamlAnnotationProcessor;
import com.github.t1.ramlap.annotations.ApiGenerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiGenerator {
    private static Type type(Class<?> klass) {
        return ReflectionProcessingEnvironment.ENV.type(klass);
    }

    private final Package pkg;

    public void generate() {
        ApiGenerate apiGenerate = pkg.getAnnotation(ApiGenerate.class);
        if (apiGenerate == null)
            return;
        for (String ramlFileName : apiGenerate.from()) {
            log.debug("generate Api from {}", ramlFileName);
            Raml raml = loadRaml(ramlFileName);
            log.debug("generate {} {}", raml.getTitle(), raml.getVersion());
            for (Resource resource : raml.getResources().values()) {
                try {
                    new ResourceGenerator(resource).generate();
                } catch (RuntimeException e) {
                    log.error("failed to generate resource " + resource.getUri() //
                            + " from " + ramlFileName + " in " + pkg, e);
                    pkg.error(RamlAnnotationProcessor.class.getName() + ": " + e.toString());
                }
            }
        }
    }

    private Raml loadRaml(String ramlFileName) {
        java.nio.file.Path path = java.nio.file.Paths.get(ramlFileName);
        ResourceLoader loader = new DefaultResourceLoader();
        YamlDocumentBuilder<Raml> builder = new YamlDocumentBuilder<>(Raml.class, loader);
        try (FileReader reader = new FileReader(path.toFile())) {
            return builder.build(reader, path.getParent().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ResourceGenerator {
        private final Resource resource;

        public ResourceGenerator(Resource resource) {
            this.resource = resource;
        }

        public void generate() {
            String typeName = typeName();
            log.debug("generate {}", typeName);
            try (TypeGenerator generator = pkg.openTypeGenerator(typeName)) {
                generator.javaDoc(resource.getDescription());
                generator.kind(INTERFACE);
                generator.annotation(type(Path.class)).set("value", resource.getUri());
                for (ActionType actionType : ActionType.values()) {
                    Action action = resource.getAction(actionType);
                    if (action == null)
                        continue;
                    MethodGenerator method = generator.addMethod(methodName(action));
                    method.javaDoc(action.getDescription());
                    method.annotation(actionAnnotation(actionType));
                    method.returnType(type(Response.class));
                }
            }
        }

        private Type actionAnnotation(ActionType actionType) {
            Class<?> type;
            try {
                type = Class.forName("javax.ws.rs." + actionType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return type(type);
        }

        private String typeName() {
            String name = resource.getDisplayName();
            if (name == null)
                name = resource.getUri().replace('/', ' ') + " Resource";
            return toUpperCamelCase(name);
        }

        private String methodName(
                Action action) {
            return toLowerCamelCase(action.getDisplayName());
        }
    }
}
