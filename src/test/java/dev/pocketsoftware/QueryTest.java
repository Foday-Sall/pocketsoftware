package dev.pocketsoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.pocketsoftware.engine.Query;
import org.junit.jupiter.api.Test;

/** Query builder: library-built JSON, URL-encoded for Appwrite. */
class QueryTest {

    @Test
    void buildsSingleEqualQuery() {
        String q = Query.equal("channel", "telegram").toParameter();
        assertEquals(1, q.split("&").length);
        // Decodes to the Appwrite query JSON with method/attribute/values.
        String json = java.net.URLDecoder.decode(q.substring("queries%5B%5D=".length()),
                java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("{\"method\":\"equal\",\"attribute\":\"channel\",\"values\":[\"telegram\"]}", json);
    }

    @Test
    void escapesQuotesInValues() {
        String q = Query.contains("fullName", "A\"ssa").toParameter();
        String json = java.net.URLDecoder.decode(q.substring("queries%5B%5D=".length()),
                java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(true, json.contains("\"A\\\"ssa\""));
    }

    @Test
    void combinesTwoQueries() {
        String q = Query.equal("channel", "telegram").and(Query.equal("channelId", "123")).toParameter();
        assertEquals(2, q.split("&").length);
    }
}
