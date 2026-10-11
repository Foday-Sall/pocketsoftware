package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;
import java.net.http.HttpResponse;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/** LLM classifier via Cloudflare Workers AI. Inactive when cf.token is blank. */
public final class CloudflareClassifier extends HttpClassifier {

    public CloudflareClassifier(Config config) {
        super(config);
    }

    private String model() {
        String m = config().get("cf.model");
        return m.isBlank() ? "@cf/meta/llama-3.3-70b-instruct-fast" : m;
    }

    @Override
    protected String prompt(String text) {
        return "Pick exactly one action for this message: MENU, REGISTER_STUDENT, LIST_STUDENTS, "
                + "FIND_STUDENT, FEES_INFO, HELP, UNKNOWN. Message: \"" + text + "\". "
                + "Reply with only JSON {\"action\":\"...\",\"confidence\":0.0}.";
    }

    @Override
    public Decision classify(String text) {
        if (config().get("cf.token").isBlank()) {
            throw new UnsupportedOperationException("Cloudflare classifier not configured");
        }
        String url = "https://api.cloudflare.com/client/v4/accounts/" + config().get("cf.accountId")
                + "/ai/run/" + model();
        String body = Json.object().set("messages", Json.array().add(Json.object()
                .set("role", "user").set("content", prompt(text)))).toString();
        try {
            HttpResponse<String> resp = send(url, body, "Bearer " + config().get("cf.token"));
            return DecisionParser.fromCloudflare(resp.body());
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            return new Decision(Actions.UNKNOWN, 0.2, null);
        }
    }
}
