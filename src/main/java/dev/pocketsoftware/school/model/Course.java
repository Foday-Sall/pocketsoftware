package dev.pocketsoftware.school.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One subject/course offered by the school. */
public record Course(String id, String name, String code, String teacher, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        if (code != null && !code.isBlank()) {
            data.put("code", code);
        }
        if (teacher != null && !teacher.isBlank()) {
            data.put("teacher", teacher);
        }
        return data;
    }

    /** Reads one database row into a record. */
    public static Course from(JsonObject row) {
        return new Course(row.getString("$id", ""), row.getString("name", "?"),
                row.getString("code", ""), row.getString("teacher", ""),
                "active");
    }
}
