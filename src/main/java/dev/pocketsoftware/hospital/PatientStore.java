package dev.pocketsoftware.hospital;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.hospital.model.Patient;
import java.util.ArrayList;
import java.util.List;

/** Saves and finds patients in the hospital database. */
public final class PatientStore {

    private final Appwrite db;

    public PatientStore(Appwrite db) {
        this.db = db;
    }

    /** Registers one patient; returns the stored record. */
    public Patient create(String fullName, String phone, String gender, Integer age, String address) {
        Patient p = new Patient(null, fullName, phone, gender, age, address, "registered");
        JsonObject row = db.createRow("hospital", "patients", p.data());
        return Patient.from(row);
    }

    /** All patients. */
    public List<Patient> all() {
        List<Patient> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("hospital", "patients", null))) {
            out.add(Patient.from(row));
        }
        return out;
    }

    /** Case-insensitive name search. */
    public List<Patient> search(String name) {
        List<Patient> out = new ArrayList<>();
        Query q = Query.contains("fullName", name);
        String lower = name == null ? "" : name.toLowerCase();
        for (var row : db.rows(db.listRows("hospital", "patients", q.toParameter()))) {
            Patient p = Patient.from(row);
            if (p.fullName().toLowerCase().contains(lower)) {
                out.add(p);
            }
        }
        return out;
    }

    /** One patient by id; null when missing. */
    public Patient byId(String id) {
        for (Patient p : all()) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }
}
