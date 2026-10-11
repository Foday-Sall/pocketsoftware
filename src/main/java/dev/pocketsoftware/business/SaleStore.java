package dev.pocketsoftware.business;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.business.model.Sale;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Records sales in the business database. */
public final class SaleStore {

    private final Appwrite db;

    public SaleStore(Appwrite db) {
        this.db = db;
    }

    /** Records one sale for today; returns the stored record. */
    public Sale record(String customerName, String item, Double amount, String paymentMethod) {
        Sale s = new Sale(null, customerName, item, amount, paymentMethod,
                LocalDate.now().toString(), "done");
        JsonObject row = db.createRow("business", "sales", s.data());
        return Sale.from(row);
    }
}
