package dev.pocketsoftware.business.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One sale record: what a customer bought, for how much. */
public record Sale(String id, String customerName, String item, Double amount,
                   String paymentMethod, String date, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customerName", customerName);
        data.put("item", item);
        data.put("amount", amount);
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            data.put("paymentMethod", paymentMethod);
        }
        if (date != null && !date.isBlank()) {
            data.put("date", date);
        }
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Sale from(JsonObject row) {
        Double amount = row.get("amount") == null || row.get("amount").isNull() ? null : row.get("amount").asDouble();
        return new Sale(row.getString("$id", ""), row.getString("customerName", "?"),
                row.getString("item", "?"), amount, row.getString("paymentMethod", ""),
                row.getString("date", ""), row.getString("status", "done"));
    }
}
