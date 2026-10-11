package dev.pocketsoftware.hospital;

/** Parses "bill <patient> <amount> <description>" and "pay <patient> <amount>". */
public final class Billing {

    private Billing() {
    }

    /** Routes a billing command; returns the chat reply. */
    static String handle(BillStore bills, String text) {
        String lower = text.trim().toLowerCase();
        if (lower.startsWith("pay ")) {
            return handlePay(bills, text.trim().substring(4));
        }
        if (lower.startsWith("bill ")) {
            return handleBill(bills, text.trim().substring(5));
        }
        return "Format: bill <patient> <amount> <description> | pay <patient> <amount>";
    }

    private static String handleBill(BillStore bills, String body) {
        String[] parts = body.split("\\s+");
        if (parts.length < 2) {
            return "Format: bill <patient> <amount> <description>";
        }
        double amount;
        try {
            amount = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return "Amount must be a number, like: bill Fatou 250 consultation";
        }
        String description = parts.length >= 3 ? parts[2] : "treatment";
        try {
            bills.add(parts[0], description, amount);
            return "✅ Bill saved: " + description + " — " + amount + " for " + parts[0] + ".";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    private static String handlePay(BillStore bills, String body) {
        String[] parts = body.split("\\s+");
        if (parts.length < 2) {
            return "Format: pay <patient> <amount>";
        }
        double amount;
        try {
            amount = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return "Amount must be a number, like: pay Fatou 100";
        }
        try {
            bills.pay(parts[0], amount, "cash");
            return "✅ Payment recorded: " + amount + " from " + parts[0] + ".";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }
}
