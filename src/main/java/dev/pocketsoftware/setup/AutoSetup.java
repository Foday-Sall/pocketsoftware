package dev.pocketsoftware.setup;

import java.nio.file.Path;
import java.util.List;

/**
 * Non-interactive provisioning for tests and scripted installs. Creates the
 * per-system databases under an optional prefix (e.g. pocket_test_) without
 * any prompts, then writes setup.completed into the given config file.
 * Credentials come from the config file.
 */
final class AutoSetup {

    /** Provisions all systems with prefixed database ids. Returns true on success. */
    static boolean run(Path configFile, List<String> systems, String prefix) {
        System.out.println("Non-interactive provisioning of " + String.join(", ", systems)
                + (prefix.isBlank() ? "" : " (databases " + prefix + "<system>)"));
        java.util.Properties props = ConfigReader.read(configFile);
        return Wizard.run(
                props.getProperty("appwrite.endpoint", ""),
                props.getProperty("appwrite.project", ""),
                props.getProperty("appwrite.key", ""),
                systems,
                system -> Schema.databaseId(system, prefix),
                "non-interactive",
                configFile);
    }
}
