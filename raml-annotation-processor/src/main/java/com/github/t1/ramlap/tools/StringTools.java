package com.github.t1.ramlap.tools;

import static java.util.Arrays.*;

import java.util.*;

public class StringTools {
    public static String camelCaseToWords(String in) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (i > 0 && Character.isUpperCase(c))
                out.append(' ');
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    private static final Set<Character> WORD_BREAKS = new HashSet<>(asList(' ', '-', '/'));

    public static String toUpperCamelCase(String in) {
        return toCamelCase(in, true);
    }

    public static String toLowerCamelCase(String in) {
        return toCamelCase(in, false);
    }

    private static String toCamelCase(String in, boolean cap) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (WORD_BREAKS.contains(c)) {
                if (out.length() > 0)
                    cap = true;
            } else if (cap) {
                cap = false;
                out.append(Character.toUpperCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
