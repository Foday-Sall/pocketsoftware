package dev.pocketsoftware.business;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.business.model.Customer;
import java.util.ArrayList;
import java.util.List;

/** Saves and finds customers in the business database. */
public final class CustomerStore {

    private final Appwrite db;

    public CustomerStore(Appwrite db) {
        this.db = db;
    }

    /** Adds one customer; returns the stored record. */
    public Customer create(String name, String phone, Double debt) {
        Customer c = new Customer(null, name, phone, "active", debt);
        JsonObject row = db.createRow("business", "biz_customers", c.data());
        return Customer.from(row);
    }

    /** All customers. */
    public List<Customer> all() {
        List<Customer> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("business", "biz_customers", null))) {
            out.add(Customer.from(row));
        }
        return out;
    }

    /** Case-insensitive name search. */
    public List<Customer> search(String name) {
        List<Customer> out = new ArrayList<>();
        String q = Query.contains("name", name).toParameter();
        String lower = name == null ? "" : name.toLowerCase();
        for (var row : db.rows(db.listRows("business", "biz_customers", q.toParameter()))) {
            Customer c = Customer.from(row);
            if (c.name().toLowerCase().contains(lower)) {
                out.add(c);
            }
        }
        return out;
    }
}
