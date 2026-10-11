package dev.pocketsoftware.setup;

import java.util.List;

/** Business tables and columns: biz_customers, sales, payments, inventory. */
final class BusinessSchema {

    static List<Schema.Table> tables() {
        return List.of(
                new Schema.Table("biz_customers", "Biz Customers", List.of(
                        col("name", "string", 128, true, null),
                        col("phone", "string", 20, false, null),
                        col("status", "string", 16, false, null),
                        col("debt", "float", 0, false, null))),
                new Schema.Table("sales", "Sales", List.of(
                        col("customerName", "string", 128, true, null),
                        col("item", "string", 255, true, null),
                        col("amount", "float", 0, true, null),
                        col("paymentMethod", "string", 32, false, null),
                        col("date", "string", 20, false, null),
                        col("status", "string", 16, false, null))),
                new Schema.Table("payments", "Payments", List.of(
                        col("customerName", "string", 128, true, null),
                        col("amount", "float", 0, true, null),
                        col("method", "string", 32, false, null),
                        col("date", "string", 20, false, null),
                        col("note", "string", 255, false, null))),
                new Schema.Table("inventory", "Inventory", List.of(
                        col("item", "string", 255, true, null),
                        col("price", "float", 0, false, null),
                        col("quantity", "integer", 0, false, null),
                        col("lowStockAt", "integer", 0, false, null),
                        col("status", "string", 16, false, null))));
    }

    private static Schema.Column col(String key, String type, int size, boolean required, String dflt) {
        return new Schema.Column(key, type, size, required, dflt);
    }

    private BusinessSchema() {
    }
}
