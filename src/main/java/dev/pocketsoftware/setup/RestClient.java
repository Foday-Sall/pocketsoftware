package dev.pocketsoftware.setup;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal Appwrite REST client for provisioning (databases, tables, columns).
 * Response helpers live in RestJson. Cloudflare blocks the default Java
 * User-Agent on this domain, so requests carry a browser User-Agent instead.
 */
final class RestClient {

    private final String base;
    private final String project;
    private final String key;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    RestClient(String base, String project, String key) {
        this.base = base;
        this.project = project;
        this.key = key;
    }

    /** Sends method + body to path; returns the parsed JSON response. */
    JsonObject send(String method, String path, JsonObject body) {
        try {
            HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(base + path))
                    .header("X-Appwrite-Project", project)
                    .header("X-Appwrite-Key", key)
                    .header("User-Agent", UserAgents.BROWSER)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30));
            if (body == null) {
                rb.GET();
            } else {
                rb.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));
            }
            HttpResponse<String> resp = http.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new RestException(method, path, resp.statusCode(), resp.body());
            }
            return resp.body().isBlank() ? Json.object() : Json.parse(resp.body()).asObject();
        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            throw new ProvisioningException("Cannot reach Appwrite at " + base + ": " + e.getMessage());
        }
    }

    JsonObject get(String path) {
        return send("GET", path, null);
    }

    JsonObject post(String path, JsonObject body) {
        return send("POST", path, body);
    }

    void delete(String path) {
        send("DELETE", path, null);
    }
}
