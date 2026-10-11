package dev.pocketsoftware.hospital.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One patient record; maps itself to and from the database row. */
public record Patient(String id, String fullName, String phone, String gender,
                      Integer age, String address, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fullName", fullName);
        if (phone != null && !phone.isBlank()) {
            data.put("phone", phone);
        }
        if (gender != null && !gender.isBlank()) {
            data.put("gender", gender);
        }
        if (age != null) {
            data.put("age", age);
        }
        if (address != null && !address.isBlank()) {
            data.put("address", address);
        }
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Patient from(JsonObject row) {
        Integer age = row.get("age") == null || row.get("age").isNull() ? null : row.get("age").asInt();
        return new Patient(row.getString("$id", ""), row.getString("fullName", "?"),
                row.getString("phone", ""), row.getString("gender", ""),
                age, row.getString("address", ""), row.getString("status", "registered"));
    }
}
