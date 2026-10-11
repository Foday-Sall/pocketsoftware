package dev.pocketsoftware.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/** Posts messages to Cloudflare Workers AI and returns the parsed response. */
public class ClefClient {

    private final String account;
    private final String token;
    private final String model;

    public ClefClient(String account, String token, String model) {
        this.account = account;
        this.token = token;
        this.model = model;
    }

    /** Runs one classification request; failures surface as IllegalStateException. */
    public JsonObject run(JsonObject payload) {
        try {
            String body = post(endpoint(), payload.toString());
            return Json.parse(body).asObject();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Clef request failed", e);
        }
    }

    /** Runs the request once; on a 429 rate limit it waits and retries one time. */
    public JsonObject runWithRetry(JsonObject payload) {
        try {
            return run(payload);
        } catch (IllegalStateException e) {
            if (e.getMessage() == null || !e.getMessage().contains("status 429")) {
                throw e;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw e;
            }
            return run(payload);
        }
    }

    /** Overridable so tests can point the client at a local server. */
    protected String endpoint() {
        return "https://api.cloudflare.com/client/v4/accounts/" + account + "/ai/run/" + model;
    }

    /** Builds the Clef decision request for one message. */
    public JsonObject payload(String text) {
        JsonObject message = Json.object().set("role", "user").set("content", text);
        JsonObject schema = Json.object()
                .set("type", "json_schema")
                .set("name", "intent")
                .set("strict", true)
                .set("schema", Json.object()
                        .set("type", "json_schema")
                        .set("name", "intent")
                        .set("strict", true)
                        .set("choice", Json.array().add(Actions.MENU).add(Actions.REGISTER_STUDENT)
                                .add(Actions.LIST_STUDENTS).add(Actions.FIND_STUDENT)
                                .add(Actions.FEES_INFO).add(Actions.HELP).add(Actions.UNKNOWN)));
        return Json.object()
                .set("messages", Json.array().add(message))
                .set("response_format", Json.object()
                        .set("type", "json_schema")
                        .set("json_schema", schema));
    }

    private String post(String url, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Clef -> status " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }
}
