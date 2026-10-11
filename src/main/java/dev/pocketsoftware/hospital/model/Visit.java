package dev.pocketsoftware.hospital.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One visit record: a patient's consultation on a date. */
public record Visit(String id, String patientId, String visitDate, String complaint,
                    String diagnosis, String treatment, String doctor, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientId", patientId);
        data.put("visitDate", visitDate);
        if (complaint != null && !complaint.isBlank()) {
            data.put("complaint", complaint);
        }
        if (diagnosis != null && !diagnosis.isBlank()) {
            data.put("diagnosis", diagnosis);
        }
        if (treatment != null && !treatment.isBlank()) {
            data.put("treatment", treatment);
        }
        if (doctor != null && !doctor.isBlank()) {
            data.put("doctor", doctor);
        }
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Visit from(JsonObject row) {
        return new Visit(row.getString("$id", ""), row.getString("patientId", ""),
                row.getString("visitDate", ""), row.getString("complaint", ""),
                row.getString("diagnosis", ""), row.getString("treatment", ""),
                row.getString("doctor", ""), row.getString("status", "open"));
    }
}
