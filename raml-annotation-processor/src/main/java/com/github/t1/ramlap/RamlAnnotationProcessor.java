package com.github.t1.ramlap;

import static javax.lang.model.SourceVersion.*;

import java.io.*;
import java.util.List;

import javax.annotation.processing.SupportedSourceVersion;
import javax.ws.rs.Path;

import org.raml.emitter.RamlEmitter;
import org.slf4j.*;

import com.github.t1.exap.*;
import com.github.t1.exap.reflection.*;
import com.github.t1.exap.reflection.Package;
import com.github.t1.ramlap.annotations.ApiGenerate;
import com.github.t1.ramlap.generator.ApiGenerator;
import com.github.t1.ramlap.scanner.RamlScanner;

import io.swagger.annotations.SwaggerDefinition;

@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationClasses({ ApiGenerate.class, SwaggerDefinition.class, Path.class })
public class RamlAnnotationProcessor extends ExtendedAbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(RamlAnnotationProcessor.class);

    public static boolean isStrict() {
        return false; // TODO this should be configurable
    }

    private final RamlScanner scanner = new RamlScanner();

    @Override
    public boolean process(Round round) throws IOException {
        log.debug("process {}", round);

        scanSwaggerDefinitions(round.typesAnnotatedWith(SwaggerDefinition.class));
        scanTypes(round.typesAnnotatedWith(Path.class));
        generateApis(round.packagesAnnotatedWith(ApiGenerate.class));

        if (round.isLast() && scanner.isWorthWriting())
            writeRaml(round);

        return false;
    }

    private void generateApis(List<Package> packages) {
        for (Package pkg : packages)
            new ApiGenerator(pkg).generate();
    }

    public void scanSwaggerDefinitions(List<Type> elements) {
        Type swaggerType = firstSwaggerDefinition(elements);
        if (swaggerType == null)
            return;
        SwaggerDefinition swaggerAnnotation = swaggerType.getAnnotation(SwaggerDefinition.class);
        scanner.scan(swaggerAnnotation);
        log.debug("processed {}", swaggerType);
    }

    private Type firstSwaggerDefinition(List<Type> types) {
        Type result = null;
        for (Type type : types)
            if (type.isPublic())
                if (result == null)
                    result = type;
                else
                    type.error("conflicting @SwaggerDefinition found besides: " + result);
            else
                type.note("skipping non-public element");
        return result;
    }

    private void scanTypes(List<Type> types) {
        for (Type type : types)
            if (!isGeneratedApi(type))
                scanner.scanJaxRsType(type);
    }

    private boolean isGeneratedApi(Type type) {
        return type.getPackage().isAnnotated(ApiGenerate.class);
    }

    private void writeRaml(Round round) throws IOException {
        String relativeName = scanner.getFileName().replace(' ', '-');
        Resource resource = round.createResource("doc", relativeName);
        log.debug("write {}", resource.getName());
        try (Writer writer = resource.openWriter()) {
            writer.write(new RamlEmitter().dump(scanner.getResult()));
        }
        log.info("created {}", resource.getName());
    }
}
