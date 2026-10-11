package dev.pocketsoftware.engine;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/** Writes audit_log and message_log entries. */
final class AuditLog {

    private final Appwrite db;

    AuditLog(Appwrite db) {
        this.db = db;
    }

    void message(String phone, String direction, String channel, String text,
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

    void audit(String actor, String action, String details) {
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
