package dev.pocketsoftware.business;

import dev.pocketsoftware.Config;
import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Classifier;
import dev.pocketsoftware.engine.ClassifierFactory;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.engine.MessageSender;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** Business module: customers, sales. The classifier decides. */
public final class App implements Module {

    private CustomerStore customers;
    private SaleStore sales;
    private Classifier classifier;
    private FlowState flow;

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
        return List.of("Add customer", "Record sale");
    }

    @Override
    public void init(Appwrite db, String channel, Config config, MessageSender sender) {
        customers = new CustomerStore(db);
        sales = new SaleStore(db);
        classifier = ClassifierFactory.create(config);
    }

    @Override
    public FlowState beginFlow() {
        flow = new AddCustomer();
        return flow;
    }

    @Override
    public String startMessage() {
        return "Let's add a customer. What is their name?";
    }

    @Override
    public String onMenu(String chatId, int entry) {
        if (entry == 2) {
            flow = new RecordSale();
            return "Recording a sale. Who is the customer? (name)";
        }
        flow = new AddCustomer();
        return startMessage();
    }

    @Override
    public boolean handles(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        if (t.contains("customer") || t.contains("sale") || t.contains("sell")
                || t.contains("inventory") || t.contains("debt")) {
            return true;
        }
        String action = classifier().classify(text).action();
        return action.equals(Actions.ADD_CUSTOMER) || action.equals(Actions.RECORD_SALE);
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (state instanceof AddCustomer c) {
            return customerStep(c, chatId, text);
        }
        if (state instanceof RecordSale s) {
            return saleStep(s, chatId, text);
        }
        return startMessage();
    }

    private String customerStep(AddCustomer c, String chatId, String text) {
        if (c.name == null) {
            c.name = text.trim();
            return "Their phone number? (or skip)";
        }
        if (c.phone == null) {
            c.phone = text.trim().equalsIgnoreCase("skip") ? "" : text.trim();
            return "Any outstanding debt? (amount in GMD, or 0)";
        }
        String debtText = text.trim();
        double debt = 0;
        if (!debtText.equals("0") && !debtText.equalsIgnoreCase("skip") && !debtText.isBlank()) {
            try {
                debt = Double.parseDouble(debtText);
            } catch (NumberFormatException e) {
                return "Debt should be a number (or 0).";
            }
        }
        try {
            var saved = customers.create(c.name, c.phone, debt);
            c.markDone();
            String reply = "Customer added: " + saved.name() + " is in the business system.";
            if (debt > 0) {
                reply += "\nOutstanding debt: " + debt + " GMD";
            }
            return reply;
        } catch (IllegalStateException e) {
            System.err.println("CUSTOMER ADD FAILED for chat " + chatId + ": " + e.getMessage());
            c.markDone();
            return "Could not reach the database right now. Please try again later.";
        }
    }

    private String saleStep(RecordSale s, String chatId, String text) {
        if (s.customerName == null) {
            s.customerName = text.trim();
            return "What did they buy? (item)";
        }
        if (s.item == null) {
            s.item = text.trim();
            return "How much did they pay? (amount in GMD)";
        }
        if (s.amountText == null) {
            try {
                s.amountText = String.valueOf(Double.parseDouble(text.trim()));
            } catch (NumberFormatException e) {
                return "Amount should be a number.";
            }
            return "Payment method? (cash / transfer / wave / momo - or skip)";
        }
        String method = text.trim().equalsIgnoreCase("skip") ? "" : text.trim();
        try {
            sales.record(s.customerName, s.item, Double.valueOf(s.amountText), method);
            s.markDone();
            return "Sale recorded: " + s.item + " for " + s.amountText + " GMD ("
                    + (method.isBlank() ? "cash" : method) + ").";
        } catch (IllegalStateException e) {
            s.markDone();
            return "Could not reach the database right now.";
        }
    }

    private Classifier classifier() {
        if (classifier == null) {
            throw new IllegalStateException("init() has not run");
        }
        return classifier;
    }
}
