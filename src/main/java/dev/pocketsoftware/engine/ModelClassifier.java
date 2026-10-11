package dev.pocketsoftware.engine;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Classifier that asks a chat-completions decision model to pick
 * the user's intent and answers as strict JSON {choice, confidence}.
 * Works with any standard chat-completions API endpoint.
 * Falls back to keywords when the model or network fails.
 */
public final class ModelClassifier implements Classifier {

    private static final List<String> CHOICES = List.of(
            Actions.MENU, Actions.REGISTER_STUDENT, Actions.LIST_STUDENTS,
            Actions.FIND_STUDENT, Actions.FEES_INFO, Actions.HELP, Actions.UNKNOWN);

    private final String url;
    private final String model;
    private final String apiKey;
    private final KeywordClassifier fallback = new KeywordClassifier();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ModelClassifier(String url, String model, String apiKey) {
        this.url = url;
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public Decision classify(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback.classify(text);
        }
        try {
            JsonObject answer = Json.parse(request(text)).asObject();
            String choice = answer.getString("choice", "")
                    .trim().toUpperCase().replace(' ', '_');
            double confidence = answer.getDouble("confidence", 0.0);
            boolean known = CHOICES.contains(choice);
            if (!known || confidence < 0.5) {
                return new Decision(Actions.UNKNOWN, confidence, null);
            }
            return new Decision(choice, confidence, slot(choice, text));
        } catch (Exception e) {
            System.err.println("Classifier model failed, falling back to keywords: " + e.getMessage());
            return fallback.classify(text);
        }
    }

    /** Keeps the keyword behaviour of extracting the search term from "find ...". */
    private String slot(String action, String text) {
        if (action.equals(Actions.FIND_STUDENT) && text != null
                && text.trim().toLowerCase().startsWith("find ")) {
            return text.trim().substring(5).trim();
        }
        return null;
    }

    /** One chat-completions call; returns the message content string. */
    private String request(String text) throws Exception {
        String system = "Classify the user message for a school bot. Reply ONLY with JSON: "
                + "{\"choice\": string, \"confidence\": number}. Choices: "
                + String.join(", ", CHOICES) + ".";
        JsonObject body = Json.object()
                .set("model", model)
                .set("messages", Json.array()
                        .add(Json.object().set("role", "system").set("content", system))
                        .add(Json.object().set("role", "user").set("content", text == null ? "" : text)))
                .set("max_tokens", 1000);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new IllegalStateException("Classifier -> status " + resp.statusCode());
        }
        JsonObject outer = Json.parse(resp.body()).asObject();
        return outer.get("choices").asArray().get(0).asObject()
                .get("message").asObject().getString("content", "");
    }
}
