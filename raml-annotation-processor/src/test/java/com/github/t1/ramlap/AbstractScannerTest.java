package com.github.t1.ramlap;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.raml.model.BddAssertions.*;

import javax.tools.Diagnostic.Kind;

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

    protected void assertMessages(int count) {
        assertThat(env.getMessager().getMessages()).hasSize(count);
    }

    protected void assertMessages(Kind kind, String... messages) {
        assertMessages(messages.length);
        assertThat(env.getMessager().getMessages(kind)).containsValue(asList(messages));
    }
}
