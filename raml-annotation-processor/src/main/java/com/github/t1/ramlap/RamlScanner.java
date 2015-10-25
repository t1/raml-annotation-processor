package com.github.t1.ramlap;

import static com.github.t1.ramlap.StringTools.*;

import java.util.TreeMap;

import org.raml.model.*;
import org.raml.model.parameter.UriParameter;
import org.slf4j.*;

import com.github.t1.exap.JavaDoc;
import com.github.t1.exap.reflection.*;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.SwaggerDefinition.Scheme;

public class RamlScanner {
    private static final Logger log = LoggerFactory.getLogger(RamlScanner.class);

    private final Raml raml = new XRaml();

    public RamlScanner() {
        raml.setTitle("");
        raml.setResources(new TreeMap<>()); // sort keys
    }

    public void scan(SwaggerDefinition swaggerDefinition) {
        String basePath = swaggerDefinition.basePath();
        if (!basePath.isEmpty()) {
            raml.setBaseUri(basePath);
            scanBasePathParams(basePath);
        }
        scan(swaggerDefinition.schemes());
        scan(swaggerDefinition.info());
    }

    private void scanBasePathParams(String basePath) {
        for (ResourcePathVariable var : ResourcePath.of(basePath).vars()) {
            String name = var.getName();
            raml.getBaseUriParameters().put(name, new UriParameter(name));
        }
    }

    private void scan(Scheme[] schemes) {
        for (Scheme scheme : schemes)
            try {
                raml.getProtocols().add(Protocol.valueOf(scheme.name()));
            } catch (IllegalArgumentException e) {
                continue;
            }
    }

    private void scan(io.swagger.annotations.Info in) {
        raml.setTitle(in.title());
        raml.setVersion((in.version().isEmpty()) ? null : in.version());
    }

    public RamlScanner scanJaxRsType(Type type) {
        log.debug("scan type {}", type.getFullName());

        scanBasic(type);

        type.accept(new TypeVisitor() {
            @Override
            public void visit(Method method) {
                new MethodScanner(raml, method).scan();
            }
        });

        log.debug("processed {}", type.getFullName());
        return this;
    }

    private void scanBasic(Type type) {
        Resource resource = ResourcePath.of(type).resource(raml);
        resource.setDisplayName(displayName(type));
        resource.setDescription(description(type));
    }

    private String displayName(Type type) {
        if (type.isAnnotated(JavaDoc.class))
            return JavaDoc.SUMMARY.apply(type.getAnnotation(JavaDoc.class));
        return camelCaseToWords(type.getSimpleName());
    }

    private String description(Type type) {
        return (type.isAnnotated(JavaDoc.class)) ? type.getAnnotation(JavaDoc.class).value() : null;
    }

    public boolean isWorthWriting() {
        return raml.getTitle() != null || !raml.getResources().isEmpty();
    }

    public Raml getResult() {
        return raml;
    }
}
