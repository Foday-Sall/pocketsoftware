package dev.pocketsoftware.channels.gowa;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/**
 * Sends WhatsApp messages through the GOWA REST API.
 *
 * POST {gowa.url}/send/message {"phone": "...", "message": "..."} with HTTP
 * Basic auth and an X-Device-Id header. Same java.net.http style as
 * engine/Appwrite — no new dependencies.
 *
 * gowa.sendEnabled=false turns the sender into a logger: replies are
 * printed, never delivered. Used for tests and for running the channel
 * before GOWA is reachable.
 */
public final class GowaSender {

    private final String baseUrl;
    private final String basicAuth;
    private final String deviceId;
    private final boolean sendEnabled;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GowaSender(String baseUrl, String user, String password, String deviceId, boolean sendEnabled) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.deviceId = deviceId;
        this.sendEnabled = sendEnabled;
    }

    /** Sends one text; returns the GOWA message id or throws with the API error. */
    public String sendText(String phone, String text) {
        if (!sendEnabled) {
            System.out.println("[dry-run] would send to " + phone + ": " + text.replace("\n", " | "));
            return "dry-run";
        }
        JsonObject body = Json.object().add("phone", phone).add("message", text);
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/send/message"))
                .header("Authorization", basicAuth)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if (deviceId != null && !deviceId.isBlank()) {
            request.header("X-Device-Id", deviceId);
        }
        try {
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("GOWA send -> " + response.statusCode() + " " + response.body());
            }
            return Json.parse(response.body()).asObject()
                    .get("results").asObject().getString("message_id", "");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("GOWA send failed: " + e.getMessage(), e);
        }
    }
}
