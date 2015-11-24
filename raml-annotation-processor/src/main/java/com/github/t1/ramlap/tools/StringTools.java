package com.github.t1.ramlap.tools;

public class StringTools {
    public static String camelCaseToWords(String string) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i > 0 && Character.isUpperCase(c))
                out.append(' ');
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }
}
