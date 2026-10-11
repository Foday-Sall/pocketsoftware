package dev.pocketsoftware.school.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One staff member, e.g. a teacher with phone and role. */
public record Staff(String id, String name, String phone, String role) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("role", role);
        if (phone != null && !phone.isBlank()) {
            data.put("phone", phone);
        }
        return data;
    }

    /** Reads one database row into a record. */
    public static Staff from(JsonObject row) {
        return new Staff(row.getString("$id", ""), row.getString("name", "?"),
                row.getString("phone", ""), row.getString("role", "teacher"));
    }
}
