package dev.pocketsoftware.school;

import dev.pocketsoftware.school.model.Student;
import java.util.List;

/** Sends the guardian an email summary after a mark is entered (best-effort). */
final class MarkNotifier {

    private final StudentStore students;
    private final dev.pocketsoftware.email.EmailSender email;

    MarkNotifier(StudentStore students, dev.pocketsoftware.email.EmailSender email) {
        this.students = students;
        this.email = email;
    }

    /** Best-effort email to the guardian; failures never break the chat flow. */
    void afterMark(String studentName, String subject, String term, int score) {
        List<Student> matches = students.search(studentName);
        if (matches.isEmpty()) {
            return;
        }
        Student student = matches.get(0);
        String to = student.guardianEmail();
        if (to == null || !to.contains("@")) {
            return;
        }
        String body = "New mark for " + student.fullName() + " ("
                + student.admissionNo() + "):\n\n"
                + subject + " — " + term + ": " + score + "/100\n\n"
                + "Sent by PocketSoftware.";
        String error = email.send(to, "New mark: " + subject + " " + term, body);
        if (error != null) {
            System.err.println("[email] failed for " + to + ": " + error);
        }
    }
}
