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
        for (Map.Entry<String, Object> e : data.entrySet()) {
            body.set(e.getKey(), String.valueOf(e.getValue()));
        }
        return request("POST", "/tablesdb/school/tables/" + table + "/rows", body);
    }

    public JsonObject listRows(String table, String query) {
        String suffix = query == null || query.isBlank() ? "" : "?" + query;
        return request("GET", "/tablesdb/school/tables/" + table + "/rows" + suffix, null);
    }

    public JsonObject updateRow(String table, String id, Map<String, Object> data) {
        JsonObject body = Json.object();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            body.set(e.getKey(), String.valueOf(e.getValue()));
        }
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
