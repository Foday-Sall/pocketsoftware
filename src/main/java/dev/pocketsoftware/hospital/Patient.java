package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.Appwrite;
import java.util.LinkedHashMap;
import java.util.Map;
import com.eclipsesource.json.JsonObject;

/** Saves patients in the Appwrite hospital database. */
public final class Patient {

    private final Appwrite db;

    public Patient(Appwrite db) {
        this.db = db;
    }

    /** Saves one registered patient; returns the created row. */
    public JsonObject create(String fullName, String phone, String complaint) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fullName", fullName);
        data.put("phone", phone);
        if (complaint != null && !complaint.isBlank()) {
            data.put("complaint", complaint);
        }
        data.put("status", "registered");
        return db.createRow("hospital", "patients", data);
    }
}
