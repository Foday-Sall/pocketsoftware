package dev.pocketsoftware.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.Config;

/** Thin java.net.http wrapper over the Appwrite TablesDB REST API. */
public final class Appwrite {

    private final String base;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public Appwrite(Config config) {
        this.base = config.get("appwrite.endpoint");
    }

    private JsonObject request(String method, String path, JsonObject body) {
        try {
            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(base + path))
                    .header("X-Appwrite-Project", System.getProperty("appwrite.project"))
                    .header("X-Appwrite-Key", System.getProperty("appwrite.key"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30));
            if (body != null) {
                rb.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));
            } else {
                rb.GET();
            }
            HttpResponse<String> resp = http.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new IllegalStateException("Appwrite " + method + " " + path + " -> " + resp.statusCode() + " " + resp.body());
            }
            return method.equals("GET") && resp.body().isEmpty() ? new JsonObject() : Json.parse(resp.body()).asObject();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Appwrite request failed: " + method + " " + path, e);
        }
    }

    /** Creates a row in the given database and table ('school' is the main database). */
    public JsonObject createRow(String database, String table, Map<String, Object> data) {
        JsonObject body = Json.object();
        // Appwrite 2.4 TablesDB: rowId is required; row fields go under "data".
        body.set("rowId", "row_" + java.util.UUID.randomUUID().toString().replace("-", ""));
        body.set("data", toPayload(data));
        return request("POST", "/tablesdb/" + database + "/tables/" + table + "/rows", body);
    }

    public JsonObject createRow(String table, Map<String, Object> data) {
        return createRow("school", table, data);
    }

    /** Updates a row in the given database and table. */
    public JsonObject updateRow(String database, String table, String rowId, Map<String, Object> data) {
        JsonObject body = Json.object();
        body.set("data", toPayload(data));
        return request("PATCH", "/tablesdb/" + database + "/tables/" + table + "/rows/" + rowId, body);
    }

    public JsonObject listRows(String database, String table, String query) {
        String suffix = query == null || query.isBlank() ? "" : "?" + query;
        return request("GET", "/tablesdb/" + database + "/tables/" + table + "/rows" + suffix, null);
    }

    public JsonObject listRows(String table, String query) {
        return listRows("school", table, query);
    }

    /** Creates an Appwrite user for an email address; returns existing user on 409. */
    public JsonObject createUser(String email, String name) {
        try {
            JsonObject body = Json.object();
            String userId = "user_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            body.set("userId", userId);
            body.set("email", email);
            if (name != null) {
                body.set("name", name);
            }
            body.set("password", java.util.UUID.randomUUID().toString());
            return request("POST", "/users", body);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("409")) {
                return findUserByEmail(email);
            }
            throw e;
        }
    }

    /** Finds an Appwrite user by exact email; null when absent. */
    public JsonObject findUserByEmail(String email) {
        JsonObject result = request("GET", "/users?q=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8), null);
        for (var row : rows(result)) {
            if (email.equalsIgnoreCase(row.getString("email", ""))) {
                return row;
            }
        }
        return null;
    }

    /** Creates an email target for a user; returns the target id. */
    public String createEmailTarget(String userId, String email) {
        JsonObject body = Json.object();
        String targetId = "tgt_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        body.set("targetId", targetId);
        body.set("providerType", "email");
        body.set("identifier", email);
        JsonObject target = request("POST", "/users/" + userId + "/targets", body);
        return target.getString("$id", targetId);
    }

    /** Sends an email through Appwrite Messaging to the given target ids. */
    public JsonObject sendEmail(String subject, String content, List<String> targetIds) {
        JsonObject body = Json.object();
        body.set("messageId", "msg_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        body.set("subject", subject);
        body.set("content", content);
        JsonArray targets = Json.array();
        for (String t : targetIds) {
            targets.add(t);
        }
        body.set("targets", targets);
        return request("POST", "/messaging/messages/email", body);
    }

    /** Preserves value types: numbers stay numbers, booleans stay booleans. */
    private static JsonObject toPayload(Map<String, Object> data) {
        JsonObject payload = Json.object();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Double) {
                payload.set(e.getKey(), (Double) v);
            } else if (v instanceof Integer) {
                payload.set(e.getKey(), (Integer) v);
            } else if (v instanceof Boolean) {
                payload.set(e.getKey(), (Boolean) v);
            } else {
                payload.set(e.getKey(), String.valueOf(v));
            }
        }
        return payload;
    }

    public List<JsonObject> rows(JsonObject listResponse) {
        var arr = listResponse.get("rows");
        List<JsonObject> out = new java.util.ArrayList<>();
        for (var v : arr.asArray().values()) {
            out.add(v.asObject());
        }
        return out;
    }
}
