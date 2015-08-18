package com.github.t1.ramlap;

import static org.raml.model.BddAssertions.*;

import org.junit.Rule;
import org.raml.model.*;

import com.github.t1.exap.reflection.*;

public class AbstractScannerTest {
    protected final ReflectionProcessingEnvironment env = new ReflectionProcessingEnvironment();

    @Rule
    public final JUnitSoftAssertions then = new JUnitSoftAssertions();

    protected Raml scanTypes(Class<?>... containers) {
        RamlScanner scanner = new RamlScanner();
        for (Class<?> container : containers) {
            Type type = env.type(container);
            scanner.scanJaxRsType(type);
        }
        return scanner.getResult();
    }

    protected Action action(Raml raml, String path, ActionType type) {
        Resource resource = raml.getResource(path);
        then(resource).as("resource " + path).isNotNull();
        Action action = resource.getAction(type);
        then(action).as("action " + type).isNotNull();
        return action;
    }
}
