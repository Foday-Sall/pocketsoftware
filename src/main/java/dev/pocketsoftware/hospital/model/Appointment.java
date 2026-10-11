package dev.pocketsoftware.hospital.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One appointment record: patient, date, time, doctor. */
public record Appointment(String id, String patientId, String date, String time,
                          String doctor, String status) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientId", patientId);
        data.put("date", date);
        if (time != null && !time.isBlank()) {
            data.put("time", time);
        }
        if (doctor != null && !doctor.isBlank()) {
            data.put("doctor", doctor);
        }
        data.put("status", status);
        return data;
    }

    /** Reads one database row into a record. */
    public static Appointment from(JsonObject row) {
        return new Appointment(row.getString("$id", ""), row.getString("patientId", ""),
                row.getString("date", ""), row.getString("time", ""),
                row.getString("doctor", ""), row.getString("status", "booked"));
    }
}
