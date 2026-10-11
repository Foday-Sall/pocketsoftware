package dev.pocketsoftware.setup;

import com.eclipsesource.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/** Safe extraction helpers for parsed REST responses. */
final class RestJson {

    /** Extracts a string field, or "" when absent. */
    static String str(JsonObject obj, String field) {
        try {
            return obj.get(field).asString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Extracts an array of objects, or an empty list when absent. */
    static List<JsonObject> array(JsonObject obj, String field) {
        List<JsonObject> out = new ArrayList<>();
        try {
            for (var value : obj.get(field).asArray().values()) {
                out.add(value.asObject());
            }
        } catch (Exception e) {
            // absent or not an array: caller sees an empty list
        }
        return out;
    }

    private RestJson() {
    }
}
