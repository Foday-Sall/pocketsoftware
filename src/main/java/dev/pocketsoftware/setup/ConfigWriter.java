package dev.pocketsoftware.setup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Rewrites selected keys in a Java properties file, preserving other lines. */
final class ConfigWriter {

    /** Rewrites or appends key=value, preserving other lines; creates the file if missing. */
    static void set(String key, String value, Path file) {
        try {
            List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();
            List<String> out = new ArrayList<>();
            boolean replaced = false;
            for (String line : lines) {
                if (isKeyLine(line, key)) {
                    if (!replaced) {
                        out.add(key + "=" + value);
                        replaced = true;
                    }
                    // drop duplicate later occurrences of the same key
                } else {
                    out.add(line);
                }
                // non-key lines pass through unchanged
            }
            if (!replaced) {
                out.add(key + "=" + value);
            }
            Files.write(file, out);
        } catch (Exception e) {
            throw new ProvisioningException("Cannot update config file " + file + ": " + e.getMessage());
        }
    }

    /** True when the file exists and is writable, or can be created in its directory. */
    static boolean isWritable(Path file) {
        if (Files.exists(file)) {
            return Files.isWritable(file);
        }
        Path dir = file.toAbsolutePath().getParent();
        return dir != null && Files.isWritable(dir);
    }

    private static boolean isKeyLine(String line, String key) {
        String trimmed = line.strip();
        return trimmed.startsWith(key + "=") || trimmed.startsWith(key + " =");
    }
}
