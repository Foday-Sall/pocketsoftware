package dev.pocketsoftware.engine;

/** Classifier that asks Clef for a choice and falls back to keywords on failure. */
public final class ClefClassifier implements Classifier {

    /** Thrown when Clef is configured without a token. */
    public static final class NotConfiguredException extends RuntimeException {
        public NotConfiguredException(String message) {
            super(message);
        }
    }

    private final ClefClient client;
    private final KeywordClassifier fallback = new KeywordClassifier();
    private final boolean configured;

    /** Test constructor: a client with an injectable endpoint. */
    public ClefClassifier(ClefClient client) {
        this.client = client;
        this.configured = true;
    }

    /** Production constructor: real Cloudflare endpoint from account, token and model. */
    public ClefClassifier(String account, String token, String model) {
        this.client = new ClefClient(account, token, model);
        this.configured = token != null && !token.isBlank();
    }

    @Override
    public Decision classify(String text) {
        if (!configured) {
            throw new NotConfiguredException("Clef token is not configured");
        }
        try {
            ClefProtocol.Answer answer =
                    ClefProtocol.parse(client.runWithRetry(client.payload(text)).toString());
            String choice = answer.choice();
            boolean known = choice.equals(Actions.MENU) || choice.equals(Actions.REGISTER_STUDENT)
                    || choice.equals(Actions.LIST_STUDENTS) || choice.equals(Actions.FIND_STUDENT)
                    || choice.equals(Actions.FEES_INFO) || choice.equals(Actions.HELP);
            if (!known || answer.confidence() < 0.5) {
                return new Decision(Actions.UNKNOWN, answer.confidence(), null);
            }
            return new Decision(choice, answer.confidence(), slot(choice, text));
        } catch (IllegalStateException e) {
            System.err.println("Clef classifier failed, falling back to keywords: " + e.getMessage());
            return fallback.classify(text);
        }
    }

    /** Keeps the keyword behaviour of extracting the search term from "find ...". */
    private String slot(String action, String text) {
        if (action.equals(Actions.FIND_STUDENT)
                && text.trim().toLowerCase().startsWith("find ")) {
            return text.trim().substring(5).trim();
        }
        return null;
    }
}
