package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;
import java.util.HashMap;
import java.util.Map;
import com.eclipsesource.json.JsonObject;

/** Routes classified messages: registration flow, menu, lookups. */
public final class Router {

    private final Map<String, ConversationState> chats = new HashMap<>();
    private final Classifier classifier;
    private final StudentStore students;
    private final AuditLog logs;
    private final String channel;

    public Router(Classifier classifier, Appwrite db, Config config, String channel) {
        this.classifier = classifier;
        this.students = new StudentStore(db);
        this.logs = new AuditLog(db);
        this.channel = channel;
    }

    /** Handles one incoming message; returns the reply text. */
    public String handle(String chatId, String text) {
        logs.message(chatId, "in", channel, text, "raw", 0.0);
        ConversationState state = chats.computeIfAbsent(chatId, id -> new ConversationState());
        if (state.registration == null) {
            state.studentId = studentId(chatId);
        }
        String reply = replyFor(state, chatId, text);
        logs.message(chatId, "out", channel, reply, "reply", 1.0);
        return reply;
    }

    private String studentId(String chatId) {
        try {
            JsonObject identity = students.identityByChannel(channel, chatId);
            return identity == null ? null : identity.getString("studentId", null);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /** One chat turn: registration flow if active, classified intent otherwise. */
    private String replyFor(ConversationState state, String chatId, String text) {
        if (state.registration != null) {
            return continueRegistration(state, chatId, text);
        }
        Decision d = classifier.classify(text);
        logs.message(chatId, "classified", channel, text, d.action(), d.confidence());
        if (d.action().equals(Actions.REGISTER_STUDENT) && state.studentId == null) {
            return startRegistration(state, chatId);
        }
        return d.confidence() < 0.5 || d.action().equals(Actions.UNKNOWN)
                ? unknownReply()
                : answerKnown(state, chatId, d);
    }

    private String startRegistration(ConversationState state, String chatId) {
        state.registration = new RegistrationState(channel, chatId);
        return "Let's register a new student. First, what is the student's full name?";
    }

    private String continueRegistration(ConversationState state, String chatId, String text) {
        RegistrationState r = state.registration;
        if (r.fullName == null) {
            r.fullName = text.trim();
            return "Got it. Which class is " + r.fullName + " in?";
        }
        if (r.classId == null) {
            r.classId = text.trim();
            return "Thanks. What is the guardian's phone number?";
        }
        if (r.guardianPhone == null) {
            r.guardianPhone = text.trim();
            return "And the guardian's name?";
        }
        if (r.guardianName == null) {
            r.guardianName = text.trim();
            return "Summary: " + r.fullName + ", class " + r.classId + ", guardian "
                    + r.guardianName + " (" + r.guardianPhone + "). Confirm? Reply yes or no.";
        }
        if (text.trim().equalsIgnoreCase("yes")) {
            return finishRegistration(state, chatId, r);
        }
        state.registration = null;
        return "Registration cancelled. Reply menu to see what I can do.";
    }

    private String finishRegistration(ConversationState state, String chatId, RegistrationState r) {
        try {
            JsonObject student = students.createStudent(r);
            String studentId = student.getString("$id", "");
            state.registration = null;
            state.studentId = studentId;
            students.createIdentity(channel, chatId, studentId);
            logs.audit(chatId, "register_student", studentId + " " + r.fullName);
            return "Student registered!";
        } catch (IllegalStateException e) {
            state.registration = null;
            return "Registration failed, please try again later.";
        }
    }

    private String answerKnown(ConversationState state, String chatId, Decision d) {
        switch (d.action()) {
            case Actions.MENU:
            case Actions.HELP:
                return menuText();
            case Actions.LIST_STUDENTS:
                return listStudents();
            case Actions.FIND_STUDENT:
                return findStudent(d.slot());
            case Actions.FEES_INFO:
                return "Fees tracking coming soon.";
            default:
                return unknownReply();
        }
    }

    private String menuText() {
        return "I can: register a student (reply 'add student'), list students ('students'), "
                + "find one ('find <name>'), or check fees ('fees').";
    }

    private String listStudents() {
        try {
            return "Students: " + String.join(", ", students.allNames());
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    private String findStudent(String name) {
        try {
            var matches = students.searchNames(name == null ? "" : name);
            return matches.isEmpty()
                    ? "No student found for '" + name + "'."
                    : "Found: " + String.join(", ", matches);
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    private String unknownReply() {
        return "I didn't catch that — reply menu to see what I can do.";
    }
}
