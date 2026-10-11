package dev.pocketsoftware.school;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.school.model.Staff;
import java.util.ArrayList;
import java.util.List;

/** Saves and lists staff members (teachers, admin). */
public final class StaffStore {

    private final Appwrite db;

    public StaffStore(Appwrite db) {
        this.db = db;
    }

    /** Adds a staff member and returns the stored record. */
    public Staff add(String name, String phone, String role) {
        Staff s = new Staff(null, name, phone, role);
        JsonObject row = db.createRow("school", "staff", s.data());
        return Staff.from(row);
    }

    /** All staff. */
    public List<Staff> all() {
        List<Staff> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("school", "staff", null))) {
            out.add(Staff.from(row));
        }
        return out;
    }
}
