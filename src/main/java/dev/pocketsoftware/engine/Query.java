package dev.pocketsoftware.engine;

import com.eclipsesource.json.Json;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Builds Appwrite REST query strings with the JSON library, not hand-made strings. */
public final class Query {

    private final List<String> encoded = new ArrayList<>();

    /** Adds one filter, e.g. Query.equal("channel", "telegram"). */
    public static Query equal(String attribute, String value) {
        return new Query().add("equal", attribute, value);
    }

    /** Adds a contains filter for text search. */
    public static Query contains(String attribute, String value) {
        return new Query().add("contains", attribute, value);
    }

    private Query add(String method, String attribute, String value) {
        String json = Json.object()
                .set("method", method)
                .set("attribute", attribute)
                .set("values", Json.array().add(value))
                .toString();
        encoded.add(URLEncoder.encode(json, StandardCharsets.UTF_8));
        return this;
    }

    /** Combines with another query for the requests "queries[]=" parameter. */
    public Query and(Query other) {
        encoded.addAll(other.encoded);
        return this;
    }

    /** Renders as "queries[]=<enc>&queries[]=<enc>", ready for the URL. */
    public String toParameter() {
        StringBuilder out = new StringBuilder();
        for (String e : encoded) {
            if (out.length() > 0) {
                out.append('&');
            }
            out.append("queries%5B%5D=").append(e);
        }
        return out.toString();
    }
}
