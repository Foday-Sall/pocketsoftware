package dev.pocketsoftware.school;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.school.model.Attendance;
import java.util.ArrayList;
import java.util.List;

/** Saves and reads attendance records in the school database. */
public final class AttendanceStore {

    private final Appwrite db;
    private final StudentStore students;

    public AttendanceStore(Appwrite db, StudentStore students) {
        this.db = db;
        this.students = students;
    }

    /** Marks one student present/absent/late for a date by student NAME. */
    public Attendance mark(String studentName, String date, String status) {
        List<dev.pocketsoftware.school.model.Student> matches = students.search(studentName);
        if (matches.isEmpty()) {
            throw new IllegalStateException("Unknown student: " + studentName);
        }
        Attendance a = new Attendance(null, matches.get(0).id(), date, status);
        JsonObject row = db.createRow("school", "attendance", a.data());
        return Attendance.from(row);
    }

    /** Attendance records for one date. */
    public List<Attendance> forDate(String date) {
        List<Attendance> out = new ArrayList<>();
        Query q = Query.equal("date", date);
        for (var row : db.rows(db.listRows("school", "attendance", q.toParameter()))) {
            out.add(Attendance.from(row));
        }
        return out;
    }

    /** Names of students marked absent on a date. */
    public List<String> absentNames(String date) {
        List<String> out = new ArrayList<>();
        for (Attendance a : forDate(date)) {
            if (a.status().equalsIgnoreCase("absent")) {
                out.add(studentName(a.studentId()));
            }
        }
        return out;
    }

    private String studentName(String id) {
        try {
            dev.pocketsoftware.school.model.Student s = students.byId(id);
            return s == null ? id : s.fullName();
        } catch (RuntimeException e) {
            return id;
        }
    }
}
