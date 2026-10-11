package dev.pocketsoftware.school.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One attendance record; maps itself to and from the database row. */
public record Attendance(String id, String studentId, String date, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", studentId);
        data.put("date", date);
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Attendance from(JsonObject row) {
        return new Attendance(row.getString("$id", ""),
                row.getString("studentId", ""), row.getString("date", ""),
                row.getString("status", "present"));
    }
}
