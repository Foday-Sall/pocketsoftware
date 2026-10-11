package dev.pocketsoftware.setup;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/** Builds the REST request for creating one column (route + body). */
final class ColumnRequests {

    /** Numeric columns: float registers as type 'double'; integer has its own route. */
    static String path(String tablePath, Schema.Column column) {
        return switch (column.type()) {
            case "float" -> tablePath + "/columns/float";
            case "integer" -> tablePath + "/columns/integer";
            default -> tablePath + "/columns/string";
        };
    }

    /** Request body: key, required, size and default for strings. */
    static JsonObject body(Schema.Column column) {
        JsonObject body = Json.object()
                .add("key", column.key())
                .add("required", column.required());
        if ("string".equals(column.type())) {
            body.add("size", column.size());
            if (column.dflt() != null) {
                body.add("default", column.dflt());
            }
        }
        return body;
    }

    private ColumnRequests() {
    }
}
