package dev.pocketsoftware.engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import com.eclipsesource.json.Json;
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

    public JsonObject getRow(String table, String id) {
        return request("GET", "/tablesdb/school/tables/" + table + "/rows/" + id, null);
    }

    public JsonObject createRow(String table, Map<String, Object> data) {
        JsonObject body = Json.object();
        // Appwrite 2.4 TablesDB: rowId is required; row fields go under "data".
        body.set("rowId", "row_" + java.util.UUID.randomUUID().toString().replace("-", ""));
        body.set("data", toPayload(data));
        return request("POST", "/tablesdb/school/tables/" + table + "/rows", body);
    }

    /** Preserves value types: numbers stay numbers, booleans stay booleans. */
    private static JsonObject toPayload(Map<String, Object> data) {
        JsonObject payload = Json.object();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Double) {
                payload.set(e.getKey(), (Double) v);
            } else if (v instanceof Float) {
                payload.set(e.getKey(), (Float) v);
            } else if (v instanceof Integer) {
                payload.set(e.getKey(), (Integer) v);
            } else if (v instanceof Long) {
                payload.set(e.getKey(), (Long) v);
            } else if (v instanceof Boolean) {
                payload.set(e.getKey(), (Boolean) v);
            } else {
                payload.set(e.getKey(), String.valueOf(v));
            }
        }
        return payload;
    }

    public JsonObject listRows(String table, String query) {
        String suffix = query == null || query.isBlank() ? "" : "?" + query;
        return request("GET", "/tablesdb/school/tables/" + table + "/rows" + suffix, null);
    }

    public JsonObject updateRow(String table, String id, Map<String, Object> data) {
        JsonObject body = Json.object();
        JsonObject payload = Json.object();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            payload.set(e.getKey(), String.valueOf(e.getValue()));
        }
        body.set("data", payload);
        return request("PATCH", "/tablesdb/school/tables/" + table + "/rows/" + id, body);
    }

    public void deleteRow(String table, String id) {
        request("DELETE", "/tablesdb/school/tables/" + table + "/rows/" + id, null);
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
