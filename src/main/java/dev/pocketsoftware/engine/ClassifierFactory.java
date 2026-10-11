package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;

/** Builds the configured classifier: model (default) or keyword fallback. */
public final class ClassifierFactory {

    /** Creates the classifier named by config key 'classifier'. */
    public static Classifier create(Config config) {
        String name = config.get("classifier").trim().toLowerCase();
        return switch (name) {
            case "", "model", "discover" -> model(config);
            case "keyword" -> new KeywordClassifier();
            default -> {
                System.out.println("Unknown classifier '" + name + "', using model.");
                yield model(config);
            }
        };
    }

    /** The model classifier: any standard chat-completions API endpoint. */
    private static Classifier model(Config config) {
        String url = config.get("discover.url");
        String modelId = config.get("discover.model");
        String key = config.get("discover.apiKey");
        if (key.isBlank()) {
            return new KeywordClassifier();
        }
        return new ModelClassifier(url, modelId, key);
    }

    private ClassifierFactory() {
    }
}
