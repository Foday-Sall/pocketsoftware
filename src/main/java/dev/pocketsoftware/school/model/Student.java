package dev.pocketsoftware.school.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One student record; maps itself to and from the database row. */
public record Student(String id, String fullName, String phone, String classId,
                      String guardianName, String guardianEmail, String admissionNo, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        if (phone != null && !phone.isBlank()) {
            data.put("phone", phone);
        }
        data.put("fullName", fullName);
        data.put("classId", classId);
        data.put("guardianName", guardianName);
        if (guardianEmail != null && !guardianEmail.isBlank()) {
            data.put("guardianEmail", guardianEmail);
        }
        data.put("admissionNo", admissionNo);
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Student from(JsonObject row) {
        return new Student(row.getString("$id", ""), row.getString("fullName", "?"),
                row.getString("phone", ""), row.getString("classId", ""),
                row.getString("guardianName", ""), row.getString("guardianEmail", ""), row.getString("admissionNo", ""),
                row.getString("status", "active"));
    }
}
