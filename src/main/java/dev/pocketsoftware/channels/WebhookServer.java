package dev.pocketsoftware.channels;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal HTTP server for inbound webhooks (GOWA).
 *
 * Sends the 200 ACK within milliseconds, then hands the raw body plus the
 * signature header to the handler on a worker thread. This order matters:
 * GOWA times out and re-sends on slow responses, which would duplicate
 * every message.
 */
public final class WebhookServer {

    private final HttpServer server;
    private final ExecutorService workers;

    public WebhookServer(String bindHost, int port, String path, Handler handler) throws IOException {
        workers = Executors.newFixedThreadPool(4);
        server = HttpServer.create(new InetSocketAddress(bindHost, port), 128);
        server.createContext(path, exchange -> {
            try (HttpExchange http = exchange) {
                byte[] body = readBody(http);
                String signature = http.getRequestHeaders().getFirst("X-Hub-Signature-256");
                respond(http, 200, "ok"); // ACK first, before any processing
                workers.submit(() -> {
                    try {
                        handler.handle(body, signature);
                    } catch (Exception e) {
                        System.err.println("Webhook handler failed: " + e.getMessage());
                    }
                });
            }
        });
        server.setExecutor(workers);
    }

    /** Processes one webhook body; the signature header may be null. */
    public interface Handler {
        void handle(byte[] body, String signature);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
        workers.shutdown();
    }

    private static byte[] readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return in.readNBytes(256 * 1024);
        }
    }

    private static void respond(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
