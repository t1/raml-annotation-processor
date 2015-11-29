package com.github.t1.ramlap.generator;

import static com.github.t1.exap.generator.TypeKind.*;
import static com.github.t1.ramlap.tools.StringTools.*;

import java.io.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.github.t1.exap.generator.*;
import com.github.t1.exap.reflection.*;
import com.github.t1.exap.reflection.Package;
import com.github.t1.ramlap.RamlAnnotationProcessor;
import com.github.t1.ramlap.annotations.ApiGenerate;
import com.github.t1.ramlap.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiGenerator {
    private static final Tag RESOURCE_PATH_TAG = new Tag("!resourcePath");

    private static Type type(Class<?> klass) {
        return ReflectionProcessingEnvironment.ENV.type(klass);
    }

    private final Package pkg;
    private final Yaml yaml = yaml();

    private static Yaml yaml() {
        Representer representer = new Representer();
        representer.addClassTag(ResourcePath2.class, RESOURCE_PATH_TAG);

        Yaml yaml = new Yaml(representer);

        yaml.addImplicitResolver(RESOURCE_PATH_TAG, Pattern.compile("/.*"), "/");
        return yaml;
    }

    public void generate() {
        ApiGenerate apiGenerate = pkg.getAnnotation(ApiGenerate.class);
        if (apiGenerate == null)
            return;
        for (String ramlFileName : apiGenerate.from()) {
            log.debug("generate Api from {}", ramlFileName);
            Raml raml = loadRaml(ramlFileName);
            log.debug("generate {} {}", raml.getTitle(), raml.getVersion());
            for (RamlResource resource : raml.getResources().values()) {
                new ResourceGenerator(resource).generate();
            }
        }
    }

    private Raml loadRaml(String ramlFileName) {
        java.nio.file.Path path = java.nio.file.Paths.get(ramlFileName);
        try (FileReader reader = new FileReader(path.toFile())) {
            return yaml.loadAs(reader, Raml.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    private class ResourceGenerator {
        private final RamlResource resource;

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

        private void generateMethods(String typeUri, TypeGenerator typeGenerator, RamlResource resource) {
            for (RamlAction action : resource.getActions().values())
                generateMethod(typeGenerator, typeUri, action);
            for (RamlResource subResource : resource.getResources().values())
                generateMethods(typeUri, typeGenerator, subResource);
        }

        private String typeName() {
            String name = resource.getDisplayName();
            if (name == null)
                name = resource.getUri().replace('/', ' ') + " Resource";
            return toUpperCamelCase(name);
        }

        private void generateMethod(TypeGenerator typeGenerator, String typeUri, RamlAction action) {
            MethodGenerator method = typeGenerator.addMethod(methodName(typeUri, action));
            method.javaDoc(action.getDescription());
            method.annotation(actionAnnotation(action.getType()));
            String methodPath = methodPath(typeUri, action);
            if (!methodPath.isEmpty())
                method.annotation(type(Path.class)).set("value", methodPath);
            method.returnType(type(Response.class));
            generateParameters(typeGenerator, method, action);
        }

        private String methodName(String typeUri, RamlAction action) {
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

        private String methodPath(String typeUri, RamlAction action) {
            String actionUri = action.getResource().getUri();
            assert actionUri.startsWith(typeUri);
            return actionUri.substring(typeUri.length());
        }

        private void generateParameters(TypeGenerator container, MethodGenerator method, RamlAction action) {
            for (Entry<String, UriParameter> entry : action.getResource().getUriParameters().entrySet()) {
                String key = entry.getKey();
                UriParameter ramlParam = entry.getValue();
                ParameterGenerator paramGenerator = method.addParameter(ramlParam.getDisplayName());
                paramGenerator.type(new TypeExpressionGenerator(container, "String"));
                paramGenerator.annotation(type(PathParam.class)).set("value", key);
            }
        }
    }
}
