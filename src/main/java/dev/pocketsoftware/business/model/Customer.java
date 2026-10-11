package dev.pocketsoftware.business.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One customer record with optional debt. */
public record Customer(String id, String name, String phone, String status, Double debt) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        if (phone != null && !phone.isBlank()) {
            data.put("phone", phone);
        }
        data.put("status", status);
        if (debt != null && debt > 0) {
            data.put("debt", debt);
        }
        return data;
    }

    /** Reads one database row into a record. */
    public static Customer from(JsonObject row) {
        Double debt = row.get("debt") == null || row.get("debt").isNull() ? null : row.get("debt").asDouble();
        return new Customer(row.getString("$id", ""), row.getString("name", "?"),
                row.getString("phone", ""), row.getString("status", "active"), debt);
    }
}
