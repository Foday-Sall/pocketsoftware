package dev.pocketsoftware.hospital;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.hospital.model.Visit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Records and lists patient visits. */
public final class VisitStore {

    private final Appwrite db;

    public VisitStore(Appwrite db) {
        this.db = db;
    }

    /** Opens a visit for today and returns the stored record. */
    public Visit open(String patientId, String complaint) {
        Visit v = new Visit(null, patientId, LocalDate.now().toString(), complaint,
                "", "", "", "open");
        JsonObject row = db.createRow("hospital", "visits", v.data());
        return Visit.from(row);
    }

    /** All visits of one patient. */
    public List<Visit> forPatient(String patientId) {
        List<Visit> out = new ArrayList<>();
        Query q = Query.equal("patientId", patientId);
        for (var row : db.rows(db.listRows("hospital", "visits", q.toParameter()))) {
            out.add(Visit.from(row));
        }
        return out;
    }
}
