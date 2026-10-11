package dev.pocketsoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.ClefClassifier;
import dev.pocketsoftware.engine.ClefClient;
import dev.pocketsoftware.engine.ClefProtocol;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Clef client and classifier behaviour against a scripted local endpoint. */
class ClefClassifierTest {

    private HttpServer server;
    private final AtomicInteger calls = new AtomicInteger();
    private String firstBody = body("FIND_STUDENT", 0.82);
    private String secondBody = body("FIND_STUDENT", 0.82);
    private int firstStatus = 200;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ai/run/@cf/cloudflare/clef", exchange -> {
            boolean first = calls.incrementAndGet() == 1;
            byte[] out = (first ? firstBody : secondBody).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(first ? firstStatus : 200, out.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(out);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private ClefClassifier localClassifier() {
        return new ClefClassifier(new ClefClient("acct", "test-token", "clef") {
            @Override
            protected String endpoint() {
                return "http://localhost:" + server.getAddress().getPort() + "/ai/run/@cf/cloudflare/clef";
            }
        });
    }

    @Test
    void parsesClefAnswer() {
        ClefProtocol.Answer a = ClefProtocol.parse(body("FIND_STUDENT", 0.82));
        assertEquals("FIND_STUDENT", a.choice());
        assertEquals(0.82, a.confidence());
    }

    @Test
    void rejectsAuthErrorEnvelope() {
        String auth = "{\"success\":false,\"result\":null,\"errors\":"
                + "[{\"code\":10000,\"message\":\"Authentication error\"}],\"messages\":[]}";
        assertThrows(IllegalStateException.class, () -> ClefProtocol.parse(auth));
    }

    @Test
    void rejectsResponseWithoutAnswers() {
        assertThrows(IllegalStateException.class, () -> ClefProtocol.parse(
                "{\"success\":true,\"result\":{\"model\":\"clef\",\"answers\":{},"
                + "\"usage\":{\"input_tokens\":1,\"output_tokens\":1}},\"errors\":[]}"));
    }

    @Test
    void classifiesOverRealHttp() {
        assertEquals(Actions.FIND_STUDENT, localClassifier().classify("who is in JSS 1").action());
    }

    @Test
    void classifiesFeesQuestion() {
        firstBody = body("FEES_INFO", 0.75);
        assertEquals(Actions.FEES_INFO, localClassifier().classify("who owes money").action());
    }

    @Test
    void retriesOnceOn429() {
        firstStatus = 429;
        ClefClassifier classifier = localClassifier();
        assertEquals(Actions.FIND_STUDENT, classifier.classify("find osman").action());
        assertEquals(2, calls.get());
    }

    @Test
    void unknownChoiceMapsToUnknown() {
        firstBody = body("GREETING", 0.9);
        assertEquals(Actions.UNKNOWN, localClassifier().classify("hey there").action());
    }

    @Test
    void lowConfidenceBecomesUnknown() {
        firstBody = body("MENU", 0.25);
        assertEquals(Actions.UNKNOWN, localClassifier().classify("hi").action());
    }

    @Test
    void blankTokenThrowsNotConfigured() {
        assertThrows(ClefClassifier.NotConfiguredException.class,
                () -> new ClefClassifier("acct", "", "clef").classify("menu"));
    }

    private static String body(String choice, double confidence) {
        return "{\"success\":true,\"result\":{\"model\":\"clef\",\"answers\":{\"intent\":"
                + "{\"type\":\"choice\",\"choice\":\"" + choice + "\",\"probabilities\":{\""
                + choice + "\":" + confidence + ",\"UNKNOWN\":" + (1 - confidence) + "},"
                + "\"confidence\":" + confidence + "}},\"usage\":{\"input_tokens\":12,"
                + "\"output_tokens\":2}},\"errors\":[]}";
    }
}
