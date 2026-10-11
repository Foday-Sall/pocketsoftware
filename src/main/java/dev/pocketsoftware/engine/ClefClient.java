package dev.pocketsoftware.engine;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends one message to Cloudflare Clef (System One API) and returns the raw
 * response. Payload format (per Cloudflare docs): model selector, state as a
 * string, questions as a typed map. Only build payload(); response parsing
 * lives in ClefProtocol.
 */
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

    /** Builds the Clef decision request for one user message. */
    public JsonObject payload(String text) {
        String actions = String.join(", ",
                Actions.MENU, Actions.REGISTER_STUDENT, Actions.LIST_STUDENTS,
                Actions.FIND_STUDENT, Actions.FEES_INFO, Actions.HELP, Actions.UNKNOWN);
        String state = "A guardian or staff member sends this message to a school chat bot. "
                + "Their message: \"" + text + "\"";
        JsonObject intent = Json.object()
                .set("type", "choice")
                .set("instructions", "Which action does the user want? Options: " + actions
                        + ". menu = see the main menu; register_student = add a new student; "
                        + "list_students = list all students; find_student = look up one student; "
                        + "fees_info = school fees or payments; help = how to use the bot; "
                        + "unknown = none of these fit.");
        JsonObject questions = Json.object().set("intent", intent);
        return Json.object()
                .set("model", "clef-flash")
                .set("state", state)
                .set("questions", questions);
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
