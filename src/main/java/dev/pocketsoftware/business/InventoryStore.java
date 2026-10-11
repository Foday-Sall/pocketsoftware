package dev.pocketsoftware.business;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.business.model.InventoryItem;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import java.util.ArrayList;
import java.util.List;

/** Saves and reads inventory records in the business database. */
public final class InventoryStore {

    private final Appwrite db;

    public InventoryStore(Appwrite db) {
        this.db = db;
    }

    /** Adds or updates one item by name. */
    public InventoryItem add(String item, Double price, Integer quantity, Integer lowStockAt) {
        InventoryItem existing = byName(item);
        if (existing != null) {
            InventoryItem updated = new InventoryItem(existing.id(), item,
                    price != null ? price : existing.price(),
                    quantity != null ? quantity : existing.quantity(),
                    lowStockAt != null ? lowStockAt : existing.lowStockAt(), existing.status());
            db.updateRow("business", "inventory", updated.id(), updated.data());
            return updated;
        }
        InventoryItem fresh = new InventoryItem(null, item, price, quantity, lowStockAt, "active");
        JsonObject row = db.createRow("business", "inventory", fresh.data());
        return InventoryItem.from(row);
    }

    /** One item by exact name; null when absent. */
    public InventoryItem byName(String item) {
        Query q = Query.equal("item", item);
        List<JsonObject> rows = db.rows(db.listRows("business", "inventory", q.toParameter()));
        return rows.isEmpty() ? null : InventoryItem.from(rows.get(0));
    }

    /** All items. */
    public List<InventoryItem> all() {
        List<InventoryItem> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("business", "inventory", null))) {
            out.add(InventoryItem.from(row));
        }
        return out;
    }

    /** Items at or below their low-stock threshold. */
    public List<InventoryItem> lowStock() {
        List<InventoryItem> out = new ArrayList<>();
        for (InventoryItem i : all()) {
            if (i.isLow()) {
                out.add(i);
            }
        }
        return out;
    }
}
