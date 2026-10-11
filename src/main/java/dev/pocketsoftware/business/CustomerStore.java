package dev.pocketsoftware.business;

import dev.pocketsoftware.engine.Appwrite;
import java.util.LinkedHashMap;
import java.util.Map;
import com.eclipsesource.json.JsonObject;

/** Creates customer rows in the business database (tablesdb/business/tables/biz_customers). */
public final class CustomerStore {

    private Appwrite db;

    /** Wires the shared database client at startup. */
    public void init(Appwrite db) {
        this.db = db;
    }

    /** Saves one customer with optional phone and debt; returns the created row. */
    public JsonObject createCustomer(String name, String phone, double debt) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        if (phone != null && !phone.isBlank()) {
            data.put("phone", phone);
        }
        data.put("status", "active");
        if (debt > 0) {
            data.put("debt", debt);
        }
        return db.createRow("business", "biz_customers", data);
    }
}
