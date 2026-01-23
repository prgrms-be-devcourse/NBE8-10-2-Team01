package com.plog.global.util;

import java.util.Locale;

public final class StringUtils {

    private StringUtils() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("\\s+", " ");
        normalized = normalized.replace(" ", "_");
        normalized = normalized.replaceAll("[^\\p{L}\\p{N}_]", "");
        return normalized;
    }
}
