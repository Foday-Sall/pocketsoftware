package dev.pocketsoftware.hospital;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.hospital.model.Bill;
import java.util.ArrayList;
import java.util.List;

/** Saves and reads bill + payment records in the hospital database. */
public final class BillStore {

    private final Appwrite db;
    private final PatientStore patients;

    public BillStore(Appwrite db, PatientStore patients) {
        this.db = db;
        this.patients = patients;
    }

    /** Creates one bill by patient NAME. */
    public Bill add(String patientName, String description, double amount) {
        List<dev.pocketsoftware.hospital.model.Patient> matches = patients.search(patientName);
        if (matches.isEmpty()) {
            throw new IllegalStateException("Unknown patient: " + patientName);
        }
        Bill b = new Bill(null, matches.get(0).id(), description, amount, "unpaid");
        JsonObject row = db.createRow("hospital", "bills", b.data());
        return Bill.from(row);
    }

    /** Records one payment for a patient NAME. */
    public void pay(String patientName, double amount, String method) {
        List<dev.pocketsoftware.hospital.model.Patient> matches = patients.search(patientName);
        if (matches.isEmpty()) {
            throw new IllegalStateException("Unknown patient: " + patientName);
        }
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("patientId", matches.get(0).id());
        data.put("amount", amount);
        if (method != null && !method.isBlank()) {
            data.put("method", method);
        }
        data.put("date", java.time.LocalDate.now().toString());
        db.createRow("hospital", "payments", data);
    }

    /** Totals (billed, paid) for one patient id. */
    public double[] totals(String patientId) {
        double billed = 0;
        double paid = 0;
        Query q = Query.equal("patientId", patientId);
        for (var row : db.rows(db.listRows("hospital", "bills", q.toParameter()))) {
            billed += row.get("amount") == null || row.get("amount").isNull() ? 0 : row.get("amount").asDouble();
        }
        for (var row : db.rows(db.listRows("hospital", "payments", q.toParameter()))) {
            paid += row.get("amount") == null || row.get("amount").isNull() ? 0 : row.get("amount").asDouble();
        }
        return new double[] {billed, paid};
    }
}
