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
                new ResourceGenerator(resource).generate();
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
            String typeUri = resource.getUri();
            try {
                String typeName = typeName();
                log.debug("generate {}", typeName);
                try (TypeGenerator typeGenerator = pkg.openTypeGenerator(typeName)) {
                    typeGenerator.javaDoc(resource.getDescription());
                    typeGenerator.kind(INTERFACE);
                    typeGenerator.annotation(type(Path.class)).set("value", typeUri);
                    generateMethods(typeUri, typeGenerator, resource);
                }
            } catch (RuntimeException e) {
                log.error("failed to generate resource " + typeUri + " in " + pkg, e);
                pkg.error(RamlAnnotationProcessor.class.getName() + ": " + e.toString());
            }
        }

        private void generateMethods(String typeUri, TypeGenerator typeGenerator, Resource resource) {
            for (Action action : resource.getActions().values())
                generateMethod(typeGenerator, typeUri, action);
            for (Resource subResource : resource.getResources().values())
                generateMethods(typeUri, typeGenerator, subResource);
        }

        private String typeName() {
            String name = resource.getDisplayName();
            if (name == null)
                name = resource.getUri().replace('/', ' ') + " Resource";
            return toUpperCamelCase(name);
        }

        private void generateMethod(TypeGenerator typeGenerator, String typeUri, Action action) {
            MethodGenerator method = typeGenerator.addMethod(methodName(typeUri, action));
            method.javaDoc(action.getDescription());
            method.annotation(actionAnnotation(action.getType()));
            String methodPath = methodPath(typeUri, action);
            if (!methodPath.isEmpty())
                method.annotation(type(Path.class)).set("value", methodPath);
            method.returnType(type(Response.class));
        }

        private String methodName(String typeUri, Action action) {
            String name = action.getDisplayName();
            if (name == null)
                name = methodPath(typeUri, action) + " " + action.getType().toString().toLowerCase();
            return toLowerCamelCase(name);
        }

        private Type actionAnnotation(ActionType actionType) {
            try {
                Class<?> type = Class.forName("javax.ws.rs." + actionType);
                return type(type);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        private String methodPath(String typeUri, Action action) {
            String actionUri = action.getResource().getUri();
            assert actionUri.startsWith(typeUri);
            return actionUri.substring(typeUri.length());
        }
    }
}
