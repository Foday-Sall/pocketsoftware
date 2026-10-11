package dev.pocketsoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Decision;
import dev.pocketsoftware.engine.DecisionParser;
import com.eclipsesource.json.Json;
import org.junit.jupiter.api.Test;

/** minimal-json parsing of sample Appwrite rows and LLM replies. */
class AppwriteJsonTest {

    @Test
    void parsesSampleAppwriteRow() {
        String body = "{\"$id\":\"row1\",\"fullName\":\"Fatou Sall\",\"classId\":\"G5\",\"phone\":\"2207000001\"}";
        var row = Json.parse(body).asObject();
        assertEquals("Fatou Sall", row.getString("fullName", "?"));
        assertEquals("G5", row.getString("classId", "?"));
    }

    @Test
    void parsesRowListWrapper() {
        String body = "{\"total\":1,\"rows\":[{\"$id\":\"a\",\"fullName\":\"Modou\"}]}";
        var list = Json.parse(body).asObject();
        assertEquals(1, list.get("rows").asArray().size());
        assertEquals("Modou", list.get("rows").asArray().get(0).asObject().getString("fullName", "?"));
    }

    @Test
    void parsesLlmDecision() {
        String content = "Sure! {\"action\":\"FIND_STUDENT\",\"confidence\":0.8,\"slot\":\"Fatou\"}";
        Decision d = DecisionParser.fromContent(content);
        assertEquals(Actions.FIND_STUDENT, d.action());
        assertEquals(0.8, d.confidence());
        assertEquals("Fatou", d.slot());
    }
}
