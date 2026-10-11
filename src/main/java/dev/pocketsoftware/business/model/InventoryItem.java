package dev.pocketsoftware.business.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One inventory item; maps itself to and from the database row. */
public record InventoryItem(String id, String item, Double price, Integer quantity, Integer lowStockAt, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("item", item);
        if (price != null) {
            data.put("price", price);
        }
        if (quantity != null) {
            data.put("quantity", quantity);
        }
        if (lowStockAt != null) {
            data.put("lowStockAt", lowStockAt);
        }
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static InventoryItem from(JsonObject row) {
        return new InventoryItem(row.getString("$id", ""), row.getString("item", "?"),
                row.get("price") == null || row.get("price").isNull() ? null : row.get("price").asDouble(),
                row.get("quantity") == null || row.get("quantity").isNull() ? null : row.get("quantity").asInt(),
                row.get("lowStockAt") == null || row.get("lowStockAt").isNull() ? null : row.get("lowStockAt").asInt(),
                row.getString("status", "active"));
    }

    /** True when the item is at or below its low-stock threshold. */
    public boolean isLow() {
        return quantity != null && lowStockAt != null && quantity <= lowStockAt;
    }
}
