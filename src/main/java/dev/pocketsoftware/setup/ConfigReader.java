package dev.pocketsoftware.setup;

import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/** Reads properties with plain parsing (java.util.Properties mangles colons). */
final class ConfigReader {

    static java.util.Properties read(java.nio.file.Path file) {
        java.util.Properties props = new java.util.Properties();
        try {
            List<String> lines = Files.exists(file) ? Files.readAllLines(file) : List.of();
            for (String line : lines) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                props.setProperty(trimmed.substring(0, eq).strip(), trimmed.substring(eq + 1).strip());
            }
        } catch (Exception e) {
            throw new ProvisioningException("Cannot read config file " + file + ": " + e.getMessage());
        }
        return props;
    }
}
