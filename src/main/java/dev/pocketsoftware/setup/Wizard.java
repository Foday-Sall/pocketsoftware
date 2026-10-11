package dev.pocketsoftware.setup;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive first-run wizard. Asks which systems this installation runs,
 * confirms the Appwrite endpoint/project/key (defaults from config), then
 * provisions the matching databases, tables and columns and marks setup
 * complete in the config file.
 */
final class Wizard {

    /** Runs the wizard; returns false when the user aborts or setup fails. */
    static boolean interactive(Path configFile) {
        System.out.println("=====================================");
        System.out.println("  PocketSoftware first-run setup");
        System.out.println("=====================================");
        System.out.println("This wizard creates the Appwrite databases, tables and");
        System.out.println("columns needed by the systems you choose. Nothing is");
        System.out.println("created before you confirm.");
        java.util.Properties props = ConfigReader.read(configFile);
        Scanner in = new Scanner(System.in);
        List<String> systems = Prompt.askSystems(in);
        String endpoint = Prompt.askValue(in, "Appwrite endpoint",
                dflt(props, "appwrite.endpoint"));
        String project = Prompt.askValue(in, "Appwrite project ID",
                dflt(props, "appwrite.project"));
        String key = Prompt.askKey(in, props.getProperty("appwrite.key", ""));
        if (key.isBlank() || key.toUpperCase().startsWith("REPLACE")) {
            System.out.println("An Appwrite API key is required to create the databases.");
            return false;
        }
        System.out.println();
        System.out.println("About to create:");
        for (String system : systems) {
            System.out.println("  " + system + " -> database '" + system + "'");
        }
        System.out.println("  at " + endpoint + " (project " + project + ")");
        String go = Prompt.askValue(in, "Proceed? (yes/no)", "yes");
        if (!go.equalsIgnoreCase("yes") && !go.equalsIgnoreCase("y")) {
            System.out.println("Aborted - nothing was created.");
            return false;
        }
        return run(endpoint, project, key, systems, system -> system, "interactive", configFile);
    }

    /** Shared provisioning run: creates databases, tables, columns, writes config. */
    static boolean run(String endpoint, String project, String key, List<String> systems,
            java.util.function.UnaryOperator<String> databaseNamer, String mode, Path configFile) {
        try {
            RestClient rest = new RestClient(endpoint, project, key);
            Provisioner provisioner = new Provisioner(rest);
            List<String> databases = new java.util.ArrayList<>();
            List<String> events = new java.util.ArrayList<>();
            for (String system : systems) {
                String databaseId = databaseNamer.apply(system);
                String status = provisioner.ensureDatabase(databaseId, Schema.databaseName(system));
                events.add("database " + databaseId + " " + status);
                databases.add(databaseId);
                events.addAll(provisioner.provisionSystem(databaseId, system));
            }
            new SetupReport(configFile).finish(mode, systems, databases, events);
            return true;
        } catch (ProvisioningException e) {
            System.out.println("Setup failed: " + e.getMessage());
            return false;
        }
    }

    private static String dflt(java.util.Properties props, String key) {
        String value = props.getProperty(key, "");
        return value == null || value.startsWith("REPLACE") ? "" : value;
    }

    private Wizard() {
    }
}
