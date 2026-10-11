package dev.pocketsoftware.setup;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Ensures all columns of one table exist and are available. */
final class ColumnEnsurer {

    private final RestClient rest;

    ColumnEnsurer(RestClient rest) {
        this.rest = rest;
    }

    /** Creates missing columns, then polls each new one until available. */
    List<String> ensureColumns(String tablePath, Schema.Table table) {
        Set<String> existing = existingKeys(tablePath);
        List<String> events = new ArrayList<>();
        List<String> created = new ArrayList<>();
        for (Schema.Column column : table.columns()) {
            if (existing.contains(column.key())) {
                events.add("column " + table.id() + "." + column.key() + " skipped");
                continue;
            }
            createColumn(tablePath, column);
            events.add("column " + table.id() + "." + column.key() + " created");
            created.add(column.key());
        }
        for (String key : created) {
            ColumnWaiter.await(rest, tablePath, table.id() + "." + key, key);
            events.add("column " + table.id() + "." + key + " available");
        }
        return events;
    }

    private void createColumn(String tablePath, Schema.Column column) {
        try {
            rest.post(ColumnRequests.path(tablePath, column), ColumnRequests.body(column));
        } catch (RestException e) {
            if (e.status == 409 || e.body.contains("already exists")) {
                return; // lost race: treat as existing, skip
            }
            throw new ProvisioningException("Creating column " + column.key() + " failed: " + e.getMessage());
        }
    }

    private Set<String> existingKeys(String tablePath) {
        Set<String> keys = new HashSet<>();
        JsonObject listing = rest.get(tablePath + "/columns");
        for (JsonObject column : RestJson.array(listing, "columns")) {
            keys.add(RestJson.str(column, "key"));
        }
        return keys;
    }
}
