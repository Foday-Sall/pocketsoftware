package dev.pocketsoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Router;
import dev.pocketsoftware.modules.Modules;
import org.junit.jupiter.api.Test;

/** Module registry: loads by id and routes menu digits to the right module. */
class ModulesTest {

    private static Modules build() {
        Modules modules = new Modules();
        modules.add(new dev.pocketsoftware.school.App());
        modules.add(new dev.pocketsoftware.hospital.App());
        return modules;
    }

    @Test
    void loadsModulesById() {
        Modules schoolOnly = Modules.load("school");
        assertEquals("school", schoolOnly.all().get(0).id());
        assertNull(schoolOnly.byId("hospital"));

        Modules all = Modules.load("school,hospital");
        assertEquals(2, all.all().size());
        assertEquals("school", all.byId("school").id());
        assertEquals("hospital", all.byId("hospital").id());
    }

    @Test
    void unknownIdsAreSkipped() {
        assertEquals(0, Modules.load("clinic").all().size());
        assertEquals(1, Modules.load("clinic,school").all().size());
    }

    @Test
    void schoolDigitsRouteToSchool() {
        Modules modules = build();
        assertSame(modules.byId("school"), modules.moduleForDigit(1));
        assertSame(modules.byId("school"), modules.moduleForDigit(4));
    }

    @Test
    void hospitalDigitRoutesToHospital() {
        Modules modules = build();
        assertSame(modules.byId("hospital"), modules.moduleForDigit(5));
        assertNull(modules.moduleForDigit(6));
    }

    @Test
    void entryNumbersResolvePerModule() {
        Modules modules = build();
        assertEquals(1, modules.entryForDigit(1));
        assertEquals(4, modules.entryForDigit(4));
        assertEquals(1, modules.entryForDigit(5));
    }

    @Test
    void digitOpensTheRightModule() {
        Router router = new Router(build(), fakeDb(), "test", testConfig());
        assertEquals("Let's register a new student. First, what is the student's full name?",
                router.handle("chat1", "1"));
        assertEquals("Let's register a new patient. What is the patient's full name?",
                router.handle("chat2", "5"));
    }

    @Test
    void flowStepsStayInTheirModule() {
        Router router = new Router(build(), fakeDb(), "test", testConfig());
        router.handle("chat3", "5");
        assertEquals("Got it. What is the patient's phone number?",
                router.handle("chat3", "Awa Diallo"));
    }

    /** Empty config: the school module falls back to the keyword classifier in tests. */
    private static Config testConfig() {
        java.util.Properties props = new java.util.Properties();
        return new Config(props);
    }

    /** Appwrite stub: empty endpoint so tests never touch the network. */
    private static Appwrite fakeDb() {
        return new Appwrite(new Config(new java.util.Properties()));
    }
}
