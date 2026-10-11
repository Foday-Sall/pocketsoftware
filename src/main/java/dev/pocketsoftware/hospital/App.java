package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** Hospital module: patient registration writing to the hospital database. */
public final class App implements Module {

    private Patient patients;

    @Override
    public String id() {
        return "hospital";
    }

    @Override
    public String title() {
        return "Hospital 🏥";
    }

    @Override
    public List<String> menuEntries() {
        return List.of("Register patient");
    }

    @Override
    public void init(Appwrite db, String channel, dev.pocketsoftware.Config config) {
        patients = new Patient(db);
    }

    @Override
    public FlowState beginFlow() {
        return new Register();
    }

    @Override
    public String startMessage() {
        return "Let's register a new patient. What is the patient's full name?";
    }

    @Override
    public String onMenu(String chatId, int entry) {
        return startMessage();
    }

    @Override
    public boolean handles(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        return t.equals("add patient") || t.contains("register patient")
                || t.contains("new patient") || t.equals("patient");
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (!(state instanceof Register flow)) {
            return startMessage();
        }
        if (flow.fullName == null) {
            flow.fullName = text.trim();
            return "Got it. What is the patient's phone number?";
        }
        if (flow.phone == null) {
            flow.phone = text.trim();
            return "Any complaint or reason for the visit? (optional — send skip to leave it out)";
        }
        String complaint = text.trim();
        if (complaint.equalsIgnoreCase("skip")) {
            complaint = "";
        }
        try {
            patients.create(flow.fullName, flow.phone, complaint);
        } catch (IllegalStateException e) {
            System.err.println("PATIENT REGISTRATION FAILED for chat " + chatId + ": " + e.getMessage());
            flow.markDone();
            return "Could not reach the database right now. Please try again later.";
        }
        flow.markDone();
        return "✅ Patient registered!\n" + flow.fullName + " is in the hospital system.";
    }
}