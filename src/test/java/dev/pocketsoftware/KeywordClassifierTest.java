package dev.pocketsoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Decision;
import dev.pocketsoftware.engine.KeywordClassifier;
import org.junit.jupiter.api.Test;

/** Keyword classifier behaviour. */
class KeywordClassifierTest {

    private final KeywordClassifier classifier = new KeywordClassifier();

    @Test
    void menuWordMapsToMenu() {
        Decision d = classifier.classify("menu");
        assertEquals(Actions.MENU, d.action());
        assertEquals(0.9, d.confidence());
    }

    @Test
    void addStudentMapsToRegister() {
        assertEquals(Actions.REGISTER_STUDENT, classifier.classify("please add student").action());
    }

    @Test
    void findCarriesSlot() {
        Decision d = classifier.classify("find Fatou");
        assertEquals(Actions.FIND_STUDENT, d.action());
        assertEquals("Fatou", d.slot());
    }

    @Test
    void gibberishIsUnknown() {
        Decision d = classifier.classify("hello there");
        assertEquals(Actions.UNKNOWN, d.action());
        assertEquals(0.2, d.confidence());
    }
}
