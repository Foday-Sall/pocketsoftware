package dev.pocketsoftware.engine;

import dev.pocketsoftware.modules.Modules;
import dev.pocketsoftware.modules.Module;

/** Builds the numbered welcome menu across all active modules. */
final class Menus {

    private Menus() {
    }

    /** Welcome text: every active module shows its own numbered menu. */
    static String welcome(Modules modules) {
        StringBuilder sb = new StringBuilder("👋 Welcome to PocketSoftware!\n\n");
        int n = 1;
        for (Module m : modules.all()) {
            sb.append(m.title()).append(" menu:\n");
            for (String entry : m.menuEntries()) {
                sb.append(n++).append(". ").append(entry).append('\n');
            }
            sb.append('\n');
        }
        sb.append("Reply with a number, or type a command like \"add student\".");
        return sb.toString();
    }
}
