package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Shared HTTP plumbing for LLM classifiers. */
public abstract class HttpClassifier implements Classifier {

    private final Config config;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected HttpClassifier(Config config) {
        this.config = config;
    }

    protected Config config() {
        return config;
    }

    /** Classification prompt shown to the model. */
    protected abstract String prompt(String text);

    protected HttpResponse<String> send(String url, String body, String bearer) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearer)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
