package com.github.t1.ramlap.scanner;

import static javax.ws.rs.core.MediaType.*;
import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;

import com.github.t1.ramlap.scanner.TypeInfo;

public class TypeInfoMediaTypePatternTest {
    private Matcher matcher(String mediaType) {
        return TypeInfo.MEDIA_TYPE_PATTERN.matcher(mediaType);
    }

    private void matches(String type, String subtype, Matcher matcher) {
        assertTrue(matcher.matches());
        assertEquals(type, matcher.group("type"));
        assertEquals(subtype, matcher.group("subtype"));
    }

    private void shouldMatch(String type, String subtype, String mediaType) {
        Matcher matcher = matcher(mediaType);

        matches(type, subtype, matcher);
    }

    @Test
    public void shouldParseSimpleMediaType() {
        shouldMatch("application", "json", APPLICATION_JSON);
    }

    @Test
    public void shouldParseMediaTypeWithParam() {
        shouldMatch("application", "json", APPLICATION_JSON + ";charset=\"utf-8\"");
    }
}
