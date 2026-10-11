package dev.pocketsoftware.setup;

import java.nio.file.Path;
import java.util.List;

/**
 * First-run setup entry point.
 *
 * TRIGGER (to be added by Main, do not re-check here): after loading config,
 * when the config lacks setup.completed=true, Main calls
 * FirstRun.maybeRun(). The wizard then asks which systems this
 * installation runs, provisions the matching per-system Appwrite databases,
 * tables and columns, and writes setup.completed=true (plus active_modules
 * and the per-system database ids) into the config file so later starts
 * skip the wizard.
 *
 * Hidden test flag: --provision school[,hospital,business|all]
 * [--database-prefix X] [--config PATH] provisions non-interactively (no
 * prompts), e.g. against throwaway pocket_test_<system> databases.
 */
public final class FirstRun {

    /** Called by Main before startup; runs the wizard once, then never again. */
    public static void maybeRun() {
        java.util.Properties props = ConfigReader.read(configPath());
        if ("true".equals(props.getProperty("setup.completed", ""))) {
            return;
        }
        System.out.println("setup.completed is not set - running first-run setup wizard.");
        if (!Wizard.interactive(configPath())) {
            System.out.println("Setup incomplete - fix the problem and restart to try again.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Path file = configPath();
        String provision = null;
        String prefix = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--provision") && i + 1 < args.length) {
                provision = args[++i];
            } else if (arg.startsWith("--provision=")) {
                provision = arg.substring("--provision=".length());
            } else if (arg.equals("--database-prefix") && i + 1 < args.length) {
                prefix = args[++i];
            } else if (arg.startsWith("--database-prefix=")) {
                prefix = arg.substring("--database-prefix=".length());
            } else if (arg.startsWith("--config=")) {
                file = Path.of(arg.substring("--config=".length()));
            } else {
                System.out.println("Usage: FirstRun [--provision school|hospital|business|all]"
                        + " [--database-prefix X] [--config PATH]");
                System.exit(2);
            }
        }
        List<String> systems = provision == null ? null : Prompt.parseSystems(provision);
        if (provision != null && systems == null) {
            System.out.println("--provision needs school, hospital, business, all"
                    + " or a combination like 1,3.");
            System.exit(2);
        }
        boolean ok = provision == null
                ? Wizard.interactive(file)
                : AutoSetup.run(file, systems, prefix);
        System.exit(ok ? 0 : 1);
    }

    /** Same config file resolution as Config.load: POCKETSOFTWARE_CONFIG or ./config.properties. */
    static Path configPath() {
        String env = System.getenv("POCKETSOFTWARE_CONFIG");
        return Path.of(env == null || env.isBlank() ? "./config.properties" : env);
    }
}
