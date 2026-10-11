package dev.pocketsoftware;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Loads config.properties from POCKETSOFTWARE_CONFIG env or ./config.properties. */
public final class Config {

    private final Properties props = new Properties();

    /* Package-private so tests in this package can build a stub config. */
    Config(Properties props) {
        this.props.putAll(props);
    }

    public static Config load() {
        String path = System.getenv().getOrDefault("POCKETSOFTWARE_CONFIG", "./config.properties");
        Path file = Path.of(path);
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read config file: " + file, e);
        }
        return new Config(props);
    }

    public String get(String key) {
        return props.getProperty(key, "");
    }

    public boolean isPlaceholder(String key) {
        String v = get(key);
        return v.isBlank()
                || v.startsWith("REPLACE")
                || v.startsWith("TEST_")
                || v.startsWith("standard_");
    }
}
