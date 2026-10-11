package dev.pocketsoftware.school;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.school.model.Grade;
import dev.pocketsoftware.school.model.Student;
import java.util.ArrayList;
import java.util.List;

/** Saves and reads grade records in the school database. */
public final class GradeStore {

    private final Appwrite db;
    private final StudentStore students;

    public GradeStore(Appwrite db, StudentStore students) {
        this.db = db;
        this.students = students;
    }

    /** Saves one mark for a student id and returns the stored record. */
    public Grade add(String studentId, String subject, String term, int score) {
        Grade g = new Grade(null, studentId, subject, term, score, "bot");
        JsonObject row = db.createRow("school", "grades", g.data());
        return Grade.from(row);
    }

    /** Saves a mark by student NAME (chat shorthand); errors if unknown. */
    public Grade addByName(String studentName, String subject, String term, int score) {
        List<Student> matches = students.search(studentName);
        if (matches.isEmpty()) {
            throw new IllegalStateException("Unknown student: " + studentName);
        }
        return add(matches.get(0).id(), subject, term, score);
    }

    /** All marks for one student. */
    public List<Grade> forStudent(String studentId) {
        List<Grade> out = new ArrayList<>();
        Query q = Query.equal("studentId", studentId);
        for (var row : db.rows(db.listRows("school", "grades", q.toParameter()))) {
            out.add(Grade.from(row));
        }
        return out;
    }
}
