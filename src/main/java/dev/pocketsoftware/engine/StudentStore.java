package dev.pocketsoftware.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.eclipsesource.json.JsonObject;

/** Creates and queries Appwrite rows for the Router. */
final class StudentStore {

    private final Appwrite db;

    StudentStore(Appwrite db) {
        this.db = db;
    }

    JsonObject createStudent(RegistrationState r) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phone", r.guardianPhone);
        data.put("fullName", r.fullName);
        data.put("classId", r.classId);
        data.put("guardianName", r.guardianName);
        data.put("admissionNo", "ADM-" + System.currentTimeMillis());
        data.put("status", "active");
        return db.createRow("students", data);
    }

    void createIdentity(String channel, String channelId, String studentId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channel", channel);
        data.put("channelId", channelId);
        data.put("studentId", studentId);
        db.createRow("identities", data);
    }

    List<String> allNames() {
        List<String> names = new ArrayList<>();
        for (JsonObject row : db.rows(db.listRows("students", null))) {
            names.add(row.getString("fullName", "?"));
        }
        return names;
    }

    /** Case-insensitive name search done client-side over a fullName-contains query. */
    List<String> searchNames(String needle) {
        List<String> names = new ArrayList<>();
        String q = "queries%5B%5D=" + query("contains", "fullName", needle);
        String lower = needle.toLowerCase();
        for (JsonObject row : db.rows(db.listRows("students", q))) {
            String name = row.getString("fullName", "?");
            if (name.toLowerCase().contains(lower)) {
                names.add(name);
            }
        }
        return names;
    }

    JsonObject identityByChannel(String channel, String channelId) {
        String q = "queries%5B%5D=" + query("equal", "channel", channel)
                + "&queries%5B%5D=" + query("equal", "channelId", channelId);
        List<JsonObject> rows = db.rows(db.listRows("identities", q));
        return rows.isEmpty() ? null : rows.get(0);
    }

    static String query(String method, String attribute, String value) {
        return java.net.URLEncoder.encode(
                "{\"method\":\"" + method + "\",\"attribute\":\"" + attribute + "\",\"values\":[\"" + value + "\"]}",
                java.nio.charset.StandardCharsets.UTF_8);
    }
}
