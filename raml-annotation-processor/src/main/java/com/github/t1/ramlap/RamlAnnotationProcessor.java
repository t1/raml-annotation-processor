package com.github.t1.ramlap;

import static javax.lang.model.SourceVersion.*;
import static javax.tools.StandardLocation.*;

import java.io.*;

import javax.annotation.processing.SupportedSourceVersion;
import javax.tools.FileObject;
import javax.ws.rs.Path;

import org.raml.emitter.RamlEmitter;
import org.slf4j.*;

import com.github.t1.exap.*;

import io.swagger.annotations.SwaggerDefinition;

@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationClasses({ Path.class })
public class RamlAnnotationProcessor extends ExtendedAbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(RamlAnnotationProcessor.class);

    private final RamlScanner scanner = new RamlScanner();

    @Override
    public boolean process(Round round) throws IOException {
        log.debug("process {}", round);

        scanner.addSwaggerDefinitions(round.typesAnnotatedWith(SwaggerDefinition.class));
        scanner.addJaxRsTypes(round.typesAnnotatedWith(Path.class));

        if (round.isLast() && scanner.isWorthWriting())
            writeRaml();

        return false;
    }

    private void writeRaml() throws IOException {
        FileObject fileObject = filer().createResource(SOURCE_OUTPUT, "", "raml.yaml");
        log.debug("write {}", fileObject.getName());
        try (Writer writer = fileObject.openWriter()) {
            writer.write(new RamlEmitter().dump(scanner.getResult()));
        }
        log.info("created {}", fileObject.getName());
    }
}
