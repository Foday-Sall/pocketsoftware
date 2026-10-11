package dev.pocketsoftware.school;

import dev.pocketsoftware.Config;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Classifier;
import dev.pocketsoftware.engine.ClassifierFactory;
import dev.pocketsoftware.engine.FlowState;
import dev.pocketsoftware.modules.Module;
import java.util.List;

/** School module: students, classes, teachers, courses, marks, PDF reports. */
public final class App implements Module {

    private StudentStore students;
    private GradeStore grades;
    private ClassStore classes;
    private StaffStore staff;
    private CourseStore courses;
    private Reports reports;
    private Classifier classifier;
    private Register register;
    private MarkNotifier notifier;

    /** Default constructor for Modules.load(); init() wires the dependencies. */
    public App() {
    }

    @Override
    public void init(Appwrite db, String channel, Config config, dev.pocketsoftware.engine.MessageSender sender) {
        students = new StudentStore(db);
        grades = new GradeStore(db, students);
        classes = new ClassStore(db);
        staff = new StaffStore(db);
        courses = new CourseStore(db);
        register = new Register(students);
        classifier = ClassifierFactory.create(config);
        reports = new Reports(students, grades, sender::sendPdf);
        notifier = new MarkNotifier(students, new dev.pocketsoftware.email.EmailSender());
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
        return List.of("Add student", "List students", "Find student", "Fees info",
                "Add mark", "Send report", "Add class", "Add teacher", "Add course");
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
            case 2 -> Queries.listText(students);
            case 3 -> "Which name should I look for? Send: find <name>";
            case 4 -> "Fees tracking coming soon.";
            case 5 -> "Send: add mark <student> <subject> <term> <score>";
            case 6 -> "Send: report <student>";
            case 7 -> "Send: add class <name> <teacherPhone>";
            case 8 -> "Send: add teacher <name> <phone>";
            default -> "Send: add course <name> <code>";
        };
    }

    @Override
    public boolean handles(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        return t.startsWith("add mark") || t.startsWith("add class") || t.startsWith("add teacher")
                || t.startsWith("add course") || t.startsWith("report") || t.contains("send me report")
                || t.equals("classes") || t.equals("teachers") || t.equals("courses")
                || classifierDecides(text);
    }

    private boolean classifierDecides(String text) {
        String action = classifier().classify(text).action();
        return action.equals("REGISTER_STUDENT") || action.equals("LIST_STUDENTS")
                || action.equals("FIND_STUDENT") || action.equals("FEES_INFO");
    }

    @Override
    public String handle(String chatId, String text, FlowState state, String userId) {
        if (state instanceof RegisterForm form) {
            return register.step(form, chatId, text);
        }
        String t = text == null ? "" : text.trim();
        String lower = t.toLowerCase();
        if (lower.startsWith("add mark")) {
            String reply = Marks.handleAddMark(grades, chatId, text);
            if (reply.startsWith("✅") && notifier != null) {
                String[] p = text.trim().split("\\s+");
                notifier.afterMark(p[2], p[3], p[4], Integer.parseInt(p[5]));
            }
            return reply;
        }
        if (lower.startsWith("add class")) {
            return Admins.handleAddClass(classes, text);
        }
        if (lower.startsWith("add teacher")) {
            return Admins.handleAddTeacher(staff, text);
        }
        if (lower.startsWith("add course")) {
            return Admins.handleAddCourse(courses, text);
        }
        if (lower.startsWith("report") || lower.contains("send me report")) {
            return reports.sendReport(chatId, reportName(t), "Term 1");
        }
        if (lower.equals("classes")) {
            return Queries.listClasses(classes);
        }
        if (lower.equals("teachers")) {
            return Queries.listTeachers(staff);
        }
        if (lower.equals("courses")) {
            return Queries.listCourses(courses);
        }
        return Queries.answer(students, classifier().classify(text));
    }

    /** Extracts the student name from "report <name>" / "send me report for <name>". */
    static String reportName(String text) {
        String t = text.trim();
        for (String prefix : new String[] {"send me report for", "send me report", "send report", "report"}) {
            if (t.toLowerCase().startsWith(prefix)) {
                return t.substring(prefix.length()).trim();
            }
        }
        return t;
    }

    private Classifier classifier() {
        if (classifier == null) {
            throw new IllegalStateException("init() has not run");
        }
        return classifier;
    }

    /** Store accessor for the router's identity lookup. */
    public StudentStore students() {
        return students;
    }

}
