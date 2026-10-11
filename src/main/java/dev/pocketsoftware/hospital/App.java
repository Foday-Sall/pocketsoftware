package dev.pocketsoftware.hospital;

import dev.pocketsoftware.Config;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Classifier;
import dev.pocketsoftware.engine.ClassifierFactory;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.engine.MessageSender;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** Hospital module: patients, visits, appointments. The classifier decides. */
public final class App implements Module {

    private PatientStore patients;
    private VisitStore visits;
    private AppointmentStore appointments;
    private Reminders reminders;
    private Classifier classifier;
    private FlowState flow;
    private String flowKind;

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
        return List.of("Register patient", "Record visit", "Book appointment");
    }

    @Override
    public void init(Appwrite db, String channel, Config config, MessageSender sender) {
        patients = new PatientStore(db);
        visits = new VisitStore(db);
        appointments = new AppointmentStore(db);
        reminders = new Reminders(appointments, patients, sender);
        reminders.startDaily(config.get("reminders.appointments").isBlank()
                ? "08:00" : config.get("reminders.appointments"));
        classifier = ClassifierFactory.create(config);
    }

    @Override
    public FlowState beginFlow() {
        flowKind = "register";
        flow = new RegisterPatient();
        return flow;
    }

    @Override
    public String startMessage() {
        return "Let's register a new patient. What is the patient's full name?";
    }

    @Override
    public String onMenu(String chatId, int entry) {
        if (entry == 2) {
            flowKind = "visit";
            flow = new BookVisit();
            return "Which patient is visiting? (name)";
        }
        if (entry == 3) {
            flowKind = "appointment";
            flow = new BookAppointment();
            return "Booking an appointment. Which patient? (name)";
        }
        flowKind = "register";
        flow = new RegisterPatient();
        return startMessage();
    }

    @Override
    public boolean handles(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        if (t.contains("patient") || t.contains("visit") || t.contains("appointment")
                || t.contains("book")) {
            return true;
        }
        String action = classifier().classify(text).action();
        return action.equals(dev.pocketsoftware.engine.Actions.REGISTER_PATIENT)
                || action.equals(dev.pocketsoftware.engine.Actions.BOOK_VISIT)
                || action.equals(dev.pocketsoftware.engine.Actions.BOOK_APPOINTMENT);
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (state instanceof RegisterPatient p) {
            return registerStep(p, chatId, text);
        }
        if (state instanceof BookVisit v) {
            return visitStep(v, chatId, text);
        }
        if (state instanceof BookAppointment a) {
            return appointmentStep(a, chatId, text);
        }
        return startMessage();
    }

    private String registerStep(RegisterPatient p, String chatId, String text) {
        if (p.fullName == null) {
            p.fullName = text.trim();
            return "Got it. What is the patient's phone number?";
        }
        if (p.phone == null) {
            p.phone = text.trim();
            return "Gender? (male / female - or skip)";
        }
        if (p.gender == null) {
            String g = text.trim().toLowerCase();
            p.gender = g.equals("skip") ? "" : g;
            return "Age? (or skip)";
        }
        if (p.ageText == null) {
            String a = text.trim();
            if (!a.equalsIgnoreCase("skip")) {
                try {
                    Integer.parseInt(a);
                    p.ageText = a;
                } catch (NumberFormatException e) {
                    return "Age should be a number (or skip).";
                }
            } else {
                p.ageText = "";
            }
            return "Address? (or skip)";
        }
        String address = text.trim();
        if (address.equalsIgnoreCase("skip")) {
            address = "";
        }
        Integer age = p.ageText.isBlank() ? null : Integer.valueOf(p.ageText);
        try {
            var saved = patients.create(p.fullName, p.phone, p.gender, age, address);
            p.markDone();
            return "Patient registered! " + saved.fullName() + " is in the hospital system.";
        } catch (IllegalStateException e) {
            System.err.println("PATIENT REGISTRATION FAILED for chat " + chatId + ": " + e.getMessage());
            p.markDone();
            return "Could not reach the database right now. Please try again later.";
        }
    }

    private String visitStep(BookVisit v, String chatId, String text) {
        if (v.patientName == null) {
            v.patientName = text.trim();
            return "What is the complaint or reason for the visit? (or skip)";
        }
        String complaint = text.trim();
        if (complaint.equalsIgnoreCase("skip")) {
            complaint = "";
        }
        var matches = patients.search(v.patientName);
        if (matches.isEmpty()) {
            v.markDone();
            return "No patient found for '" + v.patientName + "'. Register them first (menu 1).";
        }
        try {
            visits.open(matches.get(0).id(), complaint);
            v.markDone();
            return "Visit recorded for " + matches.get(0).fullName() + ".";
        } catch (IllegalStateException e) {
            v.markDone();
            return "Could not reach the database right now.";
        }
    }

    private String appointmentStep(BookAppointment a, String chatId, String text) {
        if (a.patientName == null) {
            a.patientName = text.trim();
            return "What date? (YYYY-MM-DD)";
        }
        if (a.date == null) {
            a.date = text.trim();
            return "What time? (HH:MM, or skip)";
        }
        if (a.time == null) {
            a.time = text.trim().equalsIgnoreCase("skip") ? "" : text.trim();
            return "Which doctor? (or skip)";
        }
        String doctor = text.trim().equalsIgnoreCase("skip") ? "" : text.trim();
        var matches = patients.search(a.patientName);
        if (matches.isEmpty()) {
            a.markDone();
            return "No patient found for '" + a.patientName + "'. Register them first (menu 1).";
        }
        try {
            appointments.book(matches.get(0).id(), a.date, a.time, doctor);
            a.markDone();
            return "Appointment booked for " + matches.get(0).fullName()
                    + " on " + a.date + (a.time.isBlank() ? "" : " at " + a.time) + ".";
        } catch (IllegalStateException e) {
            a.markDone();
            return "Could not reach the database right now.";
        }
    }

    private Classifier classifier() {
        if (classifier == null) {
            throw new IllegalStateException("init() has not run");
        }
        return classifier;
    }

    /** Store accessors for reminders and tests. */
    public AppointmentStore appointments() {
        return appointments;
    }

    public PatientStore patients() {
        return patients;
    }

    public Reminders reminders() {
        return reminders;
    }
}
