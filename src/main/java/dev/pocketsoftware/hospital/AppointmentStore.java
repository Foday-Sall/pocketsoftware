package dev.pocketsoftware.hospital;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.hospital.model.Appointment;
import java.util.ArrayList;
import java.util.List;

/** Books and lists appointments. */
public final class AppointmentStore {

    private final Appwrite db;

    public AppointmentStore(Appwrite db) {
        this.db = db;
    }

    /** Books one appointment and returns the stored record. */
    public Appointment book(String patientId, String date, String time, String doctor) {
        Appointment a = new Appointment(null, patientId, date, time, doctor, "booked");
        JsonObject row = db.createRow("hospital", "appointments", a.data());
        return Appointment.from(row);
    }

    /** All appointments of one patient. */
    public List<Appointment> forPatient(String patientId) {
        List<Appointment> out = new ArrayList<>();
        Query q = Query.equal("patientId", patientId);
        for (var row : db.rows(db.listRows("hospital", "appointments", q.toParameter()))) {
            out.add(Appointment.from(row));
        }
        return out;
    }

    /** All appointments on one date (for the daily reminder cron). */
    public List<Appointment> forDate(String date) {
        List<Appointment> out = new ArrayList<>();
        Query q = Query.equal("date", date);
        for (var row : db.rows(db.listRows("hospital", "appointments", q.toParameter()))) {
            out.add(Appointment.from(row));
        }
        return out;
    }
}
