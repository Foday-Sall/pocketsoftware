package dev.pocketsoftware.hospital.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One bill record; maps itself to and from the database row. */
public record Bill(String id, String patientId, String description, double amount, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientId", patientId);
        data.put("description", description);
        data.put("amount", amount);
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Bill from(JsonObject row) {
        return new Bill(row.getString("$id", ""), row.getString("patientId", ""),
                row.getString("description", ""),
                row.get("amount") == null || row.get("amount").isNull() ? 0.0 : row.get("amount").asDouble(),
                row.getString("status", "unpaid"));
    }
}
