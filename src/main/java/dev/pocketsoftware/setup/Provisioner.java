package dev.pocketsoftware.setup;

import com.eclipsesource.json.Json;
import java.util.ArrayList;
import java.util.List;

/**
 * Provisions one database per system, its tables and columns over the
 * Appwrite TablesDB REST API. Every step is idempotent: existing databases,
 * tables and columns are skipped (409 or already-present). Columns are
 * handled by ColumnEnsurer, which polls new columns until available.
 */
final class Provisioner {

    private final RestClient rest;

    Provisioner(RestClient rest) {
        this.rest = rest;
    }

    /** Creates the database if missing. Returns "created" or "skipped". */
    String ensureDatabase(String databaseId, String name) {
        try {
            rest.get("/tablesdb/" + databaseId);
            return "skipped";
        } catch (RestException e) {
            if (e.status != 404) {
                throw new ProvisioningException("Checking database " + databaseId + " failed: " + e.getMessage());
            }
        }
        try {
            rest.post("/tablesdb", Json.object().add("databaseId", databaseId).add("name", name));
            return "created";
        } catch (RestException e) {
            if (e.status == 409) {
                return "skipped"; // lost race / already created elsewhere
            }
            throw new ProvisioningException("Creating database " + databaseId + " failed: " + e.getMessage());
        }
    }

    /** Ensures every table of the system exists and its columns are available. */
    List<String> provisionSystem(String databaseId, String system) {
        List<String> events = new ArrayList<>();
        for (Schema.Table table : Schema.tablesFor(system)) {
            events.add("table " + table.id() + " " + ensureTable(databaseId, table));
            events.addAll(new ColumnEnsurer(rest).ensureColumns(tablePath(databaseId, table.id()), table));
        }
        return events;
    }

    /** Creates the table if missing. Returns "created" or "skipped". */
    private String ensureTable(String databaseId, Schema.Table table) {
        String path = tablePath(databaseId, table.id());
        try {
            rest.get(path);
            return "skipped";
        } catch (RestException e) {
            if (e.status != 404) {
                throw new ProvisioningException("Checking table " + databaseId + "/" + table.id() + " failed: " + e.getMessage());
            }
        }
        try {
            rest.post(parentPath(path), Json.object().add("tableId", table.id()).add("name", table.name()));
            return "created";
        } catch (RestException e) {
            if (e.status == 409) {
                return "skipped";
            }
            throw new ProvisioningException("Creating table " + table.id() + " failed: " + e.getMessage());
        }
    }

    private static String tablePath(String databaseId, String tableId) {
        return "/tablesdb/" + databaseId + "/tables/" + tableId;
    }

    private static String parentPath(String path) {
        return path.substring(0, path.lastIndexOf('/'));
    }
}
