package com.arckenver.towny.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.arckenver.towny.TownyPlugin;

/**
 * Utility that mirrors the wording used by the Spigot Towny plugin by reading
 * an exported {@code en_US.yml} language file. The importer performs a
 * best-effort fuzzy match against the existing Sponge translation strings so
 * we can keep the familiar constant based API while still inheriting the
 * canonical text from upstream Towny.
 */
public final class SpigotLanguageImporter {

    private static final double MAX_DISTANCE = 0.45D;

    private SpigotLanguageImporter() {
    }

    public static Map<String, String> load(File rootDir, Map<String, String> defaults) {
        File langDir = new File(rootDir, "spigot-lang");
        File langFile = new File(langDir, "en_US.yml");
        if (!langFile.exists()) {
            return Collections.emptyMap();
        }

        Map<String, String> spigotStrings;
        try {
            spigotStrings = parseSimpleYaml(langFile);
        } catch (IOException e) {
            TownyPlugin.getLogger().warn("Failed to read Spigot Towny language file: {}", e.getMessage());
            return Collections.emptyMap();
        }

        if (spigotStrings.isEmpty() || defaults.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> normalizedSpigot = new HashMap<>();
        for (Entry<String, String> entry : spigotStrings.entrySet()) {
            String sanitized = sanitize(entry.getValue());
            if (!sanitized.isEmpty()) {
                normalizedSpigot.put(entry.getKey(), sanitized);
            }
        }

        Map<String, String> overrides = new HashMap<>();
        for (Entry<String, String> entry : defaults.entrySet()) {
            String field = entry.getKey();
            String fallback = entry.getValue();
            if (fallback == null || fallback.isEmpty()) {
                continue;
            }

            String sanitizedFallback = sanitize(fallback);
            if (sanitizedFallback.isEmpty()) {
                continue;
            }

            String bestKey = null;
            double bestScore = Double.MAX_VALUE;

            // Attempt direct key heuristics first.
            String heuristicKey = heuristicKey(field, spigotStrings);
            if (heuristicKey != null) {
                bestKey = heuristicKey;
                bestScore = 0D;
            } else {
                for (Entry<String, String> candidate : normalizedSpigot.entrySet()) {
                    double distance = normalizedDistance(sanitizedFallback, candidate.getValue());
                    if (distance < bestScore) {
                        bestScore = distance;
                        bestKey = candidate.getKey();
                    }
                }
            }

            if (bestKey != null && bestScore <= MAX_DISTANCE) {
                overrides.put(field, spigotStrings.get(bestKey));
            }
        }

        if (!overrides.isEmpty()) {
            TownyPlugin.getLogger().info("Loaded {} strings from Spigot Towny language pack.", overrides.size());
        }

        return overrides;
    }

    private static Map<String, String> parseSimpleYaml(File file) throws IOException {
        Map<String, String> values = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int colon = trimmed.indexOf(':');
                if (colon <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, colon).trim();
                if (key.isEmpty()) {
                    continue;
                }

                String rawValue = trimmed.substring(colon + 1).trim();
                if (rawValue.isEmpty()) {
                    values.put(key, "");
                    continue;
                }

                // Handle inline comments.
                int commentIndex = rawValue.indexOf(" #");
                if (commentIndex >= 0) {
                    rawValue = rawValue.substring(0, commentIndex).trim();
                }

                // Remove optional quoting.
                if ((rawValue.startsWith("\"") && rawValue.endsWith("\""))
                        || (rawValue.startsWith("'") && rawValue.endsWith("'")) && rawValue.length() >= 2) {
                    rawValue = rawValue.substring(1, rawValue.length() - 1);
                }

                rawValue = rawValue.replace("''", "'");
                values.put(key, rawValue);
            }
        }

        return values;
    }

    private static String sanitize(String input) {
        String lower = input.replace('&', ' ').toLowerCase(Locale.ROOT);
        return lower.replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String heuristicKey(String field, Map<String, String> spigotStrings) {
        String canonical = field.toLowerCase(Locale.ROOT);
        if (spigotStrings.containsKey(canonical)) {
            return canonical;
        }

        String msgKey = "msg_" + canonical;
        if (spigotStrings.containsKey(msgKey)) {
            return msgKey;
        }

        String errKey = "msg_err_" + canonical.replace("error_", "");
        if (spigotStrings.containsKey(errKey)) {
            return errKey;
        }

        String infoKey = "msg_info_" + canonical.replace("info_", "");
        if (spigotStrings.containsKey(infoKey)) {
            return infoKey;
        }

        String helpKey = "msg_help_" + canonical.replace("help_", "");
        if (spigotStrings.containsKey(helpKey)) {
            return helpKey;
        }

        return null;
    }

    private static double normalizedDistance(String left, String right) {
        if (left.equals(right)) {
            return 0D;
        }
        int distance = StringUtils.getLevenshteinDistance(left, right, 1000);
        if (distance < 0) {
            return 1D;
        }
        int max = Math.max(left.length(), right.length());
        if (max == 0) {
            return 0D;
        }
        return (double) distance / (double) max;
    }
}
