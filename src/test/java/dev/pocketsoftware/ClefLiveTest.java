package dev.pocketsoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.ClefClassifier;
import dev.pocketsoftware.engine.Decision;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Live end-to-end Clef calls. Runs only when CLEF_ACCOUNT and CLEF_TOKEN are set in the
 * environment; skipped otherwise so the normal build never needs secrets.
 */
class ClefLiveTest {

    private record Expected(String message, String action) { }

    private static final List<Expected> CASES = List.of(
            new Expected("menu", Actions.MENU),
            new Expected("good morning, I need help", Actions.MENU),
            new Expected("I want to register my daughter Amina", Actions.REGISTER_STUDENT),
            new Expected("add student", Actions.REGISTER_STUDENT),
            new Expected("show me all the students", Actions.LIST_STUDENTS),
            new Expected("list students", Actions.LIST_STUDENTS),
            new Expected("who is Fatou", Actions.FIND_STUDENT),
            new Expected("find the student called osman", Actions.FIND_STUDENT),
            new Expected("who owes money", Actions.FEES_INFO),
            new Expected("how much does JSS 1A still owe in school fees", Actions.FEES_INFO),
            new Expected("what is the capital of France", Actions.UNKNOWN),
            new Expected("lol ok", Actions.UNKNOWN));

    @Test
    void classifiesRealMessages() {
        String account = System.getenv("CLEF_ACCOUNT");
        String token = System.getenv("CLEF_TOKEN");
        assumeTrue(token != null && !token.isBlank(), "CLEF_TOKEN not set - live test skipped");
        ClefClassifier classifier = new ClefClassifier(account, token, "@cf/cloudflare/clef");
        StringBuilder mismatches = new StringBuilder();
        for (Expected c : CASES) {
            Decision d = classifier.classify(c.message());
            System.out.println("CLEF: \"" + c.message() + "\" -> " + d.action()
                    + " confidence=" + d.confidence());
            if (!d.action().equals(c.action())) {
                mismatches.append('"').append(c.message()).append("\" expected ")
                        .append(c.action()).append(" got ").append(d.action())
                        .append(" (").append(d.confidence()).append(")\n");
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.toString());
    }
}
