package dev.pocketsoftware.hospital.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One prescription record; maps itself to and from the database row. */
public record Prescription(String id, String patientId, String visitId, String medicine,
                           String dosage, String instructions, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientId", patientId);
        if (visitId != null && !visitId.isBlank()) {
            data.put("visitId", visitId);
        }
        data.put("medicine", medicine);
        if (dosage != null && !dosage.isBlank()) {
            data.put("dosage", dosage);
        }
        if (instructions != null && !instructions.isBlank()) {
            data.put("instructions", instructions);
        }
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Prescription from(JsonObject row) {
        return new Prescription(row.getString("$id", ""), row.getString("patientId", ""),
                row.getString("visitId", ""), row.getString("medicine", ""),
                row.getString("dosage", ""), row.getString("instructions", ""),
                row.getString("status", "active"));
    }
}
