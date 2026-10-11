package dev.pocketsoftware.engine;

/** Writes audit_log and message_log entries. */
public final class AuditLog {

    private final Appwrite db;

    public AuditLog(Appwrite db) {
        this.db = db;
    }

    public void message(String phone, String direction, String channel, String text,
                        String intent, double confidence) {
        try {
            db.createRow("message_log", java.util.Map.of(
                    "phone", phone,
                    "direction", direction,
                    "channel", channel,
                    "text", text,
                    "intent", intent,
                    "confidence", confidence));
        } catch (IllegalStateException e) {
            // Logging must never break the chat flow.
        }
    }

    public void audit(String actor, String action, String details) {
        try {
            db.createRow("audit_log", java.util.Map.of(
                    "actor", actor,
                    "action", action,
                    "details", details));
        } catch (IllegalStateException e) {
            // Logging must never break the chat flow.
        }
    }
}
