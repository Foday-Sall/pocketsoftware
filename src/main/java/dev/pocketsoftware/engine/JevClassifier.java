package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;
import java.net.http.HttpResponse;
import com.eclipsesource.json.Json;

/** LLM classifier via OpenRouter (Jev). Inactive when jev.apiKey is blank. */
public final class JevClassifier extends HttpClassifier {

    public JevClassifier(Config config) {
        super(config);
    }

    private String model() {
        String m = config().get("jev.model");
        return m.isBlank() ? "typesafe/jev-1.13" : m;
    }

    @Override
    protected String prompt(String text) {
        return "Classify this message into exactly one action: MENU, REGISTER_STUDENT, "
                + "LIST_STUDENTS, FIND_STUDENT, FEES_INFO, HELP, UNKNOWN. "
                + "Message: \"" + text + "\". Reply with only JSON {\"action\":\"...\",\"confidence\":0.0}.";
    }

    @Override
    public Decision classify(String text) {
        if (config().get("jev.apiKey").isBlank()) {
            throw new UnsupportedOperationException("Jev classifier not configured");
        }
        String body = Json.object()
                .set("model", model())
                .set("messages", Json.array().add(Json.object()
                        .set("role", "user").set("content", prompt(text))))
                .toString();
        try {
            HttpResponse<String> resp = send(config().get("jev.url"), body,
                    "Bearer " + config().get("jev.apiKey"));
            return DecisionParser.fromOpenRouter(resp.body());
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            return new Decision(Actions.UNKNOWN, 0.2, null);
        }
    }
}
