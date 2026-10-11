package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Query;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.eclipsesource.json.JsonObject;

/** Saves and finds students in the Appwrite school database. */
public final class Student {

    private final Appwrite db;

    public Student(Appwrite db) {
        this.db = db;
    }

    /** Saves one registered student; returns the created row. */
    public JsonObject createStudent(String fullName, String classId, String guardianPhone,
                                    String guardianName) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phone", guardianPhone);
        data.put("fullName", fullName);
        data.put("classId", classId);
        data.put("guardianName", guardianName);
        data.put("admissionNo", "ADM-" + System.currentTimeMillis());
        data.put("status", "active");
        return db.createRow("school", "students", data);
    }

    public void createIdentity(String channel, String channelId, String studentId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channel", channel);
        data.put("channelId", channelId);
        data.put("studentId", studentId);
        db.createRow("school", "identities", data);
    }

    public List<String> allNames() {
        List<String> names = new ArrayList<>();
        for (JsonObject row : db.rows(db.listRows("school", "students", null))) {
            names.add(row.getString("fullName", "?"));
        }
        return names;
    }

    /** Case-insensitive name search done client-side over a fullName-contains query. */
    public List<String> searchNames(String needle) {
        List<String> names = new ArrayList<>();
        String q = Query.contains("fullName", needle).toParameter();
        String lower = needle.toLowerCase();
        for (JsonObject row : db.rows(db.listRows("school", "students", q))) {
            String name = row.getString("fullName", "?");
            if (name.toLowerCase().contains(lower)) {
                names.add(name);
            }
        }
        return names;
    }

    public JsonObject identityByChannel(String channel, String channelId) {
        Query q = Query.equal("channel", channel).and(Query.equal("channelId", channelId));
        List<JsonObject> rows = db.rows(db.listRows("school", "identities", q.toParameter()));
        return rows.isEmpty() ? null : rows.get(0);
    }
}
