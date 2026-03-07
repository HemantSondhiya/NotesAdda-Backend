package com.example.NotsHub.util;

import java.text.Normalizer;
import java.util.function.Predicate;

/**
 * Utility class for generating URL-friendly slugs from entity names.
 */
public final class SlugUtil {

    private SlugUtil() {
    }

    /**
     * Generates a URL-friendly slug from the given text.
     * Example: "Stanford University" → "stanford-university"
     */
    public static String generateSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String slug = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")  // remove accents
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")  // remove non-alphanumeric except spaces and hyphens
                .replaceAll("[\\s-]+", "-")         // replace spaces/hyphens with single hyphen
                .replaceAll("^-|-$", "");            // trim leading/trailing hyphens

        return slug;
    }

    /**
     * Generates a unique slug by appending a numeric suffix if needed.
     * Example: if "stanford-university" exists, returns "stanford-university-2"
     */
    public static String makeUnique(String baseSlug, Predicate<String> existsCheck) {
        if (!existsCheck.test(baseSlug)) {
            return baseSlug;
        }

        int suffix = 2;
        String candidate;
        do {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        } while (existsCheck.test(candidate));

        return candidate;
    }
}
