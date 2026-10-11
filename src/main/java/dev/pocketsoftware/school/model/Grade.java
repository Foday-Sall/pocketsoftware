package dev.pocketsoftware.school.model;

import com.eclipsesource.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One mark entry: a student's score in a subject for a term. */
public record Grade(String id, String studentId, String subject, String term,
                    int score, String enteredBy) {

    /** Column values for saving. */
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", studentId);
        data.put("subject", subject);
        data.put("term", term);
        data.put("score", score);
        if (enteredBy != null && !enteredBy.isBlank()) {
            data.put("enteredBy", enteredBy);
        }
        return data;
    }

    /** Reads one database row into a record. */
    public static Grade from(JsonObject row) {
        return new Grade(row.getString("$id", ""), row.getString("studentId", ""),
                row.getString("subject", "?"), row.getString("term", "?"),
                row.getInt("score", 0), row.getString("enteredBy", ""));
    }
}
