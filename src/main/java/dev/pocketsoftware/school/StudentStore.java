package dev.pocketsoftware.school;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import dev.pocketsoftware.school.model.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Saves and finds students in the Appwrite school database, using real Student records. */
public final class StudentStore {

    private final Appwrite db;

    public StudentStore(Appwrite db) {
        this.db = db;
    }

    /** Saves one registered student (phone column = the guardian's WhatsApp number). */
    public Student create(String fullName, String guardianPhone, String classId, String guardianName) {
        Student s = new Student(null, fullName, guardianPhone, classId,
                guardianName, null, "ADM-" + System.currentTimeMillis(), "active");
        Map<String, Object> data = s.data();
        String id = db.createRow("school", "students", data).getString("$id", "");
        return new Student(id, s.fullName(), s.phone(), s.classId(),
                s.guardianName(), null, s.admissionNo(), s.status());
    }

    public void createIdentity(String channel, String channelId, String studentId) {
        db.createRow("school", "identities", Map.of(
                "channel", channel, "channelId", channelId, "studentId", studentId));
    }

    /** All students as records. */
    public List<Student> all() {
        List<Student> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("school", "students", null))) {
            out.add(Student.from(row));
        }
        return out;
    }

    /** Names only, for the quick list reply. */
    public List<String> allNames() {
        return all().stream().map(Student::fullName).collect(Collectors.toList());
    }

    /** Case-insensitive name search done client-side over a fullName-contains query. */
    public List<Student> search(String needle) {
        List<Student> out = new ArrayList<>();
        String q = Query.contains("fullName", needle).toParameter();
        String lower = needle.toLowerCase();
        for (var row : db.rows(db.listRows("school", "students", q))) {
            Student s = Student.from(row);
            if (s.fullName().toLowerCase().contains(lower)) {
                out.add(s);
            }
        }
        return out;
    }

    /** One student by row id; null when missing. */
    public Student byId(String id) {
        for (Student s : all()) {
            if (s.id().equals(id)) {
                return s;
            }
        }
        return null;
    }

    public JsonObject identityByChannel(String channel, String channelId) {
        Query q = Query.equal("channel", channel).and(Query.equal("channelId", channelId));
        List<JsonObject> rows = db.rows(db.listRows("school", "identities", q.toParameter()));
        return rows.isEmpty() ? null : rows.get(0);
    }
}
