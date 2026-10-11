package dev.pocketsoftware.business;

import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** Business module: customer registration with debt, writing to the business database. */
public final class App implements Module {

    private final Customer customers = new Customer();

    @Override
    public String id() {
        return "business";
    }

    @Override
    public String title() {
        return "Business";
    }

    @Override
    public List<String> menuEntries() {
        return List.of("Add customer");
    }

    @Override
    public void init(Appwrite db, String channel, Config config, dev.pocketsoftware.engine.MessageSender sender) {
        customers.init(db);
    }

    @Override
    public FlowState beginFlow() {
        return new Register();
    }

    @Override
    public String startMessage() {
        return "Let's add a new customer. What is the customer's name?";
    }

    @Override
    public String onMenu(String chatId, int entry) {
        return startMessage();
    }

    @Override
    public boolean handles(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        return t.equals("add customer") || t.contains("new customer")
                || t.contains("register customer") || t.equals("customer");
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (!(state instanceof Register flow)) {
            return startMessage();
        }
        if (flow.name == null) {
            flow.name = text.trim();
            return "Got it. What is the customer's phone number? (optional — send skip to leave it out)";
        }
        if (flow.phone == null) {
            String p = text.trim();
            flow.phone = p.equalsIgnoreCase("skip") ? "" : p;
            return "How much does the customer owe? A number, e.g. 250 or 1499.50 "
                    + "(send 0 if nothing).";
        }
        String raw = text.trim().replace(",", "");
        double debt;
        try {
            debt = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return "That doesn't look like a number. How much does the customer owe? "
                    + "E.g. 250 or 1499.50 (send 0 if nothing).";
        }
        try {
            customers.create(flow.name, flow.phone, debt);
        } catch (IllegalStateException e) {
            System.err.println("CUSTOMER REGISTRATION FAILED for chat " + chatId + ": " + e.getMessage());
            flow.markDone();
            return "Could not reach the database right now. Please try again later.";
        }
        flow.markDone();
        return "✅ Customer added!\n" + flow.name + " is in the business system."
                + (debt > 0 ? "\nOutstanding debt: " + debt : "");
    }
}