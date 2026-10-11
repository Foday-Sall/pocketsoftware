package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Decision;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.engine.KeywordClassifier;
import dev.pocketsoftware.engine.RegistrationState;
import dev.pocketsoftware.engine.StudentStore;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** School module: approved student registration flow plus student lookups. */
public final class SchoolModule implements Module {

    private StudentStore students;
    private KeywordClassifier classifier;
    private RegistrationFlow flow;
    private SchoolQueries queries;

    /** Default constructor for Modules.load(); init() wires the dependencies. */
    public SchoolModule() {
    }

    @Override
    public void init(Appwrite db, String channel) {
        students = new StudentStore(db);
        classifier = new KeywordClassifier();
        flow = new RegistrationFlow(students);
        queries = new SchoolQueries(students);
    }

    @Override
    public String id() {
        return "school";
    }

    @Override
    public String title() {
        return "School";
    }

    @Override
    public List<String> menuEntries() {
        return List.of("Add student", "List students", "Find student", "Fees info");
    }

    @Override
    public FlowState beginFlow() {
        return new RegistrationState();
    }

    @Override
    public String startMessage() {
        return "Let's register a new student. First, what is the student's full name?";
    }

    @Override
    public String onMenu(String chatId, int entry) {
        switch (entry) {
            case 1: return startMessage();
            case 2: return queries.list();
            case 3: return "Which name should I look for? Send: find <name>";
            default: return queries.fees();
        }
    }

    @Override
    public boolean handles(String text) {
        String a = classifier().classify(text).action();
        return a.equals(Actions.REGISTER_STUDENT) || a.equals(Actions.LIST_STUDENTS)
                || a.equals(Actions.FIND_STUDENT) || a.equals(Actions.FEES_INFO);
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (state instanceof RegistrationState reg) {
            return flow.step(reg, chatId, text);
        }
        return queries.answer(classifier().classify(text));
    }

    private KeywordClassifier classifier() {
        if (classifier == null) {
            classifier = new KeywordClassifier();
        }
        return classifier;
    }

    /** Store accessor for the router's identity lookup. */
    public StudentStore students() {
        return students;
    }
}
