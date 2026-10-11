package dev.pocketsoftware.setup;

import java.nio.file.Path;
import java.util.List;

/** Prints the final setup summary and marks the config as complete. */
final class SetupReport {

    private final Path configFile;

    SetupReport(Path configFile) {
        this.configFile = configFile;
    }

    /** Prints what was created/skipped and writes setup.completed into config. */
    void finish(String mode, List<String> systems, List<String> databaseIds, List<String> events) {
        System.out.println();
        System.out.println("Setup report");
        System.out.println("------------");
        System.out.println("Mode: " + mode);
        System.out.println("Systems: " + String.join(", ", systems));
        System.out.println("Databases: " + String.join(", ", databaseIds));
        int created = 0;
        int skipped = 0;
        for (String event : events) {
            System.out.println("  " + event);
            if (event.endsWith("created")) {
                created++;
            } else if (event.endsWith("skipped")) {
                skipped++;
            }
        }
        System.out.println("Created: " + created + ", skipped (already present): " + skipped);
        markComplete(systems, databaseIds);
    }

    /** Marks setup complete in the config file, so the next start skips the wizard. */
    private void markComplete(List<String> systems, List<String> databaseIds) {
        if (!ConfigWriter.isWritable(configFile)) {
            System.out.println("Config file is not writable: " + configFile);
            System.out.println("Add these lines manually to prevent the wizard from running again:");
            System.out.println("  setup.completed=true");
            System.out.println("  active_modules=" + String.join(",", systems));
            return;
        }
        ConfigWriter.set("setup.completed", "true", configFile);
        ConfigWriter.set("active_modules", String.join(",", systems), configFile);
        int i = 0;
        for (String system : systems) {
            ConfigWriter.set("setup.db." + system, databaseIds.get(i), configFile);
            i++;
        }
        System.out.println("Config updated: " + configFile + " (setup.completed=true)");
    }
}
