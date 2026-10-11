package dev.pocketsoftware.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry of active modules, loaded from the config key 'active_modules'. */
public final class Modules {

    private final Map<String, Module> byId = new LinkedHashMap<>();

    /** Adds a module; ids are unique, first registration wins. */
    public void add(Module module) {
        if (module == null || module.id() == null || module.id().isBlank()) {
            throw new IllegalArgumentException("Module needs a non-blank id");
        }
        byId.putIfAbsent(module.id(), module);
    }

    /** Active modules in config order. */
    public List<Module> all() {
        return new ArrayList<>(byId.values());
    }

    /** Looks a module up by id; null when not registered. */
    public Module byId(String id) {
        return id == null ? null : byId.get(id);
    }

    /** The module a global menu digit belongs to, or null when out of range. */
    public Module moduleForDigit(int digit) {
        int remaining = digit;
        for (Module m : byId.values()) {
            int count = m.menuEntries().size();
            if (remaining <= count) {
                return m;
            }
            remaining -= count;
        }
        return null;
    }

    /** The 1-based entry number of a global menu digit within its module, or 0. */
    public int entryForDigit(int digit) {
        int remaining = digit;
        for (Module m : byId.values()) {
            int count = m.menuEntries().size();
            if (remaining <= count) {
                return remaining;
            }
            remaining -= count;
        }
        return 0;
    }

    /** Builds the registry for comma-separated ids (unknown ids are skipped). */
    public static Modules load(String csv) {
        Modules modules = new Modules();
        for (String raw : (csv == null || csv.isBlank() ? "school" : csv).split(",")) {
            String id = raw.trim().toLowerCase();
            if (id.equals("school")) {
                modules.add(new dev.pocketsoftware.school.App());
            } else if (id.equals("hospital")) {
                modules.add(new dev.pocketsoftware.hospital.App());
            } else if (id.equals("business")) {
                modules.add(new dev.pocketsoftware.business.App());
            }
        }
        return modules;
    }
}
