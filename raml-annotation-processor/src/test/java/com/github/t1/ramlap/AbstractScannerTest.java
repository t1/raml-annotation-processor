package com.github.t1.ramlap;

import static java.util.Arrays.*;
import static javax.tools.Diagnostic.Kind.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.raml.model.BddAssertions.*;

import java.util.*;

import javax.tools.Diagnostic.Kind;

import org.junit.*;
import org.raml.model.*;

import com.github.t1.exap.reflection.*;

import io.swagger.annotations.SwaggerDefinition;

public abstract class AbstractScannerTest {
    protected final ReflectionProcessingEnvironment env = new ReflectionProcessingEnvironment();

    @Rule
    public final JUnitSoftAssertions then = new JUnitSoftAssertions();

    protected Raml scanTypes(Class<?>... containers) {
        RamlScanner scanner = new RamlScanner();
        for (Class<?> container : containers) {
            Type type = env.type(container);
            if (type.isAnnotated(SwaggerDefinition.class))
                scanner.scan(type.getAnnotation(SwaggerDefinition.class));
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


    public static <T> Set<T> asSet(@SuppressWarnings("unchecked") T... items) {
        return new LinkedHashSet<>(asList(items));
    }

    private int expectedMessageCount = 0;

    protected void assertWarning(ReflectionMessageTarget target, String message) {
        assertTrue(target.getMessages(WARNING).contains(message));
        expectedMessageCount++;
    }

    protected void assertMessages(Kind kind, String... messages) {
        expectedMessageCount += messages.length;
        assertThat(env.getMessager().getMessages(kind)).containsValue(asList(messages));
    }

    @After
    public void assertNoUnexpectedMessages() {
        assertThat(env.getMessager().getMessages()).hasSize(expectedMessageCount);
    }
}
