package dev.pocketsoftware.engine;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/** Parses Cloudflare Clef choice answers. Package-visible data, public parse entry. */
public final class ClefProtocol {

    /** A Clef choice answer: the chosen label and its confidence. */
    public record Answer(String choice, double confidence) {
    }

    private ClefProtocol() {
    }

    /** Parses a Clef response envelope; throws when Clef rejected or answered nothing. */
    public static Answer parse(String body) {
        JsonObject json = Json.parse(body).asObject();
        if (!json.getBoolean("success", false)) {
            throw new IllegalStateException("Clef error: " + body);
        }
        JsonObject result = json.get("result").asObject();
        JsonObject answers = result.get("answers").asObject();
        if (answers.isEmpty()) {
            throw new IllegalStateException("Clef returned no answers: " + body);
        }
        JsonObject intent = answers.get("intent").asObject();
        String choice = intent.getString("choice", "");
        double confidence = intent.getDouble("confidence", 0.0);
        return new Answer(choice, confidence);
    }
}
