package dev.pocketsoftware.school.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One school class, e.g. Grade 5A with its teacher's phone. */
public record SchoolClass(String id, String name, String teacherPhone, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        if (teacherPhone != null && !teacherPhone.isBlank()) {
            data.put("teacherPhone", teacherPhone);
        }
        return data;
    }

    /** Reads one database row into a record. */
    public static SchoolClass from(JsonObject row) {
        return new SchoolClass(row.getString("$id", ""), row.getString("name", "?"),
                row.getString("teacherPhone", ""), "active");
    }
}
