package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.DailyJob;
import dev.pocketsoftware.school.model.Grade;
import dev.pocketsoftware.school.model.Student;
import java.util.List;

/** Builds and sends report cards: on demand in chat, and on a daily cron. */
public final class Reports {

    /** One PDF delivery: chatId, filename, bytes. */
    public interface Sender {
        void send(String chatId, String filename, byte[] pdf);
    }

    private final StudentStore students;
    private final GradeStore grades;
    private final Sender sender;

    public Reports(StudentStore students, GradeStore grades, Sender sender) {
        this.students = students;
        this.grades = grades;
        this.sender = sender;
    }

    /** Sends one student's term report to a chat. Returns the reply text. */
    public String sendReport(String chatId, String studentName, String term) {
        List<Student> matches = students.search(studentName);
        if (matches.isEmpty()) {
            return "No student found for '" + studentName + "'.";
        }
        Student s = matches.get(0);
        List<Grade> marks = grades.forStudent(s.id());
        if (marks.isEmpty()) {
            return "No marks yet for " + s.fullName() + ".";
        }
        byte[] pdf = ReportPdf.render(s, term, marks);
        sender.send(chatId, "report-" + s.fullName().replaceAll("\\s+", "-") + ".pdf", pdf);
        return "📄 Report sent for " + s.fullName() + " (" + term + ").";
    }

    /** Starts the daily cron that sends all students' reports to the school admin chat. */
    public void startDaily(String hhmm, String adminChatId, String term) {
        new DailyJob().startAt(hhmm, () -> {
            try {
                for (Student s : students.all()) {
                    List<Grade> marks = grades.forStudent(s.id());
                    if (!marks.isEmpty()) {
                        byte[] pdf = ReportPdf.render(s, term, marks);
                        sender.send(adminChatId, "report-" + s.fullName().replaceAll("\\s+", "-") + ".pdf", pdf);
                    }
                }
            } catch (Exception e) {
                System.err.println("Daily report job failed: " + e.getMessage());
            }
        });
    }
}
