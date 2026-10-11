package dev.pocketsoftware.hospital;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.hospital.model.Prescription;
import java.util.ArrayList;
import java.util.List;

/** Saves and reads prescription records in the hospital database. */
public final class PrescriptionStore {

    private final Appwrite db;
    private final PatientStore patients;

    public PrescriptionStore(Appwrite db, PatientStore patients) {
        this.db = db;
        this.patients = patients;
    }

    /** Adds one prescription by patient NAME. */
    public Prescription add(String patientName, String medicine, String dosage, String instructions) {
        List<dev.pocketsoftware.hospital.model.Patient> matches = patients.search(patientName);
        if (matches.isEmpty()) {
            throw new IllegalStateException("Unknown patient: " + patientName);
        }
        Prescription p = new Prescription(null, matches.get(0).id(), null, medicine, dosage, instructions, "active");
        JsonObject row = db.createRow("hospital", "prescriptions", p.data());
        return Prescription.from(row);
    }

    /** All prescriptions for one patient id. */
    public List<Prescription> forPatient(String patientId) {
        List<Prescription> out = new ArrayList<>();
        Query q = Query.equal("patientId", patientId);
        for (var row : db.rows(db.listRows("hospital", "prescriptions", q.toParameter()))) {
            out.add(Prescription.from(row));
        }
        return out;
    }
}
