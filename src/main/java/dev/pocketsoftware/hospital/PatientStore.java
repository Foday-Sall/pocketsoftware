package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.Appwrite;
import java.util.LinkedHashMap;
import java.util.Map;
import com.eclipsesource.json.JsonObject;

/** Creates patient rows in the hospital database (tablesdb/hospital/tables/patients). */
public final class PatientStore {

    private final Appwrite db;

    public PatientStore(Appwrite db) {
        this.db = db;
    }

    /** Saves one registered patient; returns the created row. */
    public JsonObject createPatient(String fullName, String phone, String complaint) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fullName", fullName);
        data.put("phone", phone);
        if (complaint != null && !complaint.isBlank()) {
            data.put("address", complaint);
        }
        data.put("status", "registered");
        return db.createRow("hospital", "patients", data);
    }
}
