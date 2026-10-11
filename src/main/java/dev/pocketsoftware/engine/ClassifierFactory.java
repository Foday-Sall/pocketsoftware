package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;

/** Builds the configured classifier: jev (default), clef, or keyword. */
public final class ClassifierFactory {

    /** Creates the classifier named by config key 'classifier'. */
    public static Classifier create(Config config) {
        String name = config.get("classifier").trim().toLowerCase();
        return switch (name) {
            case "", "jev" -> jev(config);
            case "clef" -> clef(config);
            case "keyword" -> new KeywordClassifier();
            default -> {
                System.out.println("Unknown classifier '" + name + "', using jev.");
                yield jev(config);
            }
        };
    }

    private static Classifier jev(Config config) {
        String url = config.get("jev.url");
        String model = config.get("jev.model");
        String key = config.get("jev.apiKey");
        if (key.isBlank()) {
            return new KeywordClassifier();
        }
        return new JevClassifier(url, model, key);
    }

    private static Classifier clef(Config config) {
        String token = config.get("clef.token");
        if (token.isBlank()) {
            return new KeywordClassifier();
        }
        return new ClefClassifier(config.get("clef.accountId"), token,
                config.get("clef.model").isBlank() ? "clef-flash" : config.get("clef.model"));
    }

    private ClassifierFactory() {
    }
}
