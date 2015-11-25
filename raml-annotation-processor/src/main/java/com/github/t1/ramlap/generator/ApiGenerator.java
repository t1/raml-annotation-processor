package com.github.t1.ramlap.generator;

import static com.github.t1.ramlap.tools.StringTools.*;
import static org.raml.model.ActionType.*;

import java.io.*;
import java.nio.file.*;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.raml.model.Raml;
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
            new Generator(ramlFileName).generate();
        }
    }

    private class Generator {
        private final String ramlFileName;
        private final Raml raml;

        public Generator(String ramlFileName) {
            this.ramlFileName = ramlFileName;
            this.raml = loadRaml(ramlFileName);
        }

        private Raml loadRaml(String ramlFileName) {
            Path path = Paths.get(ramlFileName);
            ResourceLoader loader = new DefaultResourceLoader();
            YamlDocumentBuilder<Raml> builder = new YamlDocumentBuilder<>(Raml.class, loader);
            try (FileReader reader = new FileReader(path.toFile())) {
                return builder.build(reader, path.getParent().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void generate() {
            for (Resource resource : raml.getResources().values()) {
                String typeName = toUpperCamelCase(raml.getTitle());
                log.debug("generate {}", typeName);
                try (TypeGenerator generator = pkg.openTypeGenerator(typeName)) {
                    MethodGenerator method = generator.addMethod(methodName(resource));
                    method.annotation(type(GET.class));
                    method.returnType(type(Response.class));
                    method.body("return null;");
                } catch (RuntimeException e) {
                    log.error("failed to process api {} in {}", ramlFileName, pkg);
                    pkg.error(RamlAnnotationProcessor.class.getName() + ": " + e.toString());
                }
            }
        }

        private String methodName(Resource resource) {
            return toLowerCamelCase(resource.getAction(GET).getDisplayName());
        }
    }
}
