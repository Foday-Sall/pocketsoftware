package dev.pocketsoftware.engine;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/** Extracts {action, confidence, slot} from LLM API responses. */
public final class DecisionParser {

    private DecisionParser() {
    }

    /** OpenRouter chat completion: content is nested under choices[0].message.content. */
    public static Decision fromOpenRouter(String body) {
        JsonObject root = Json.parse(body).asObject();
        String content = root.get("choices").asArray().get(0).asObject()
                .get("message").asObject().get("content").asString();
        return fromContent(content);
    }

    /** Cloudflare AI run: result is a JSON string field on the root object. */
    public static Decision fromCloudflare(String body) {
        JsonObject root = Json.parse(body).asObject();
        JsonValue result = root.get("result");
        String content = result != null && result.isString() ? result.asString() : root.toString();
        return fromContent(content);
    }

    /** Pulls {action, confidence} out of a model's text reply. */
    public static Decision fromContent(String content) {
        String json = content.substring(content.indexOf('{'), content.lastIndexOf('}') + 1);
        JsonObject obj = Json.parse(json).asObject();
        String action = obj.getString("action", Actions.UNKNOWN);
        double confidence = obj.getDouble("confidence", 0.2);
        String slot = obj.getString("slot", null);
        return new Decision(action, confidence, slot);
    }
}
