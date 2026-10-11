package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Classifier;
import dev.pocketsoftware.engine.ClassifierFactory;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** School module: student registration flow plus lookups, classifier decides. */
public final class App implements Module {

    private Student students;
    private Classifier classifier;
    private Register register;
    private Queries queries;

    /** Default constructor for Modules.load(); init() wires the dependencies. */
    public App() {
    }

    @Override
    public void init(Appwrite db, String channel, dev.pocketsoftware.Config config) {
        students = new Student(db);
        classifier = ClassifierFactory.create(config);
        register = new Register(students);
        queries = new Queries(students);
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
        return new RegisterForm();
    }

    @Override
    public String startMessage() {
        return "Let's register a new student. First, what is the student's full name?";
    }

    @Override
    public String onMenu(String chatId, int entry) {
        return switch (entry) {
            case 1 -> startMessage();
            case 2 -> queries.list();
            case 3 -> "Which name should I look for? Send: find <name>";
            default -> queries.fees();
        };
    }

    @Override
    public boolean handles(String text) {
        String action = classifier().classify(text).action();
        return action.equals(Actions.REGISTER_STUDENT) || action.equals(Actions.LIST_STUDENTS)
                || action.equals(Actions.FIND_STUDENT) || action.equals(Actions.FEES_INFO);
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (state instanceof RegisterForm form) {
            return register.step(form, chatId, text);
        }
        return queries.answer(classifier().classify(text));
    }

    private Classifier classifier() {
        if (classifier == null) {
            throw new IllegalStateException("init() has not run");
        }
        return classifier;
    }

    /** Store accessor for the router's identity lookup. */
    public Student students() {
        return students;
    }
}