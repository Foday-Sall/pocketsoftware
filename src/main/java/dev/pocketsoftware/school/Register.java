package dev.pocketsoftware.school;

/** Runs the student registration conversation; mutates the flow state. */
final class Register {

    private final Student students;

    Register(Student students) {
        this.students = students;
    }

    /** One step of the flow; returns the reply text. */
    String step(RegisterForm r, String chatId, String text) {
        if (r.fullName == null) {
            r.fullName = text.trim();
            return "Got it. Which class is " + r.fullName + " in?";
        }
        if (r.classId == null) {
            r.classId = text.trim();
            return "Thanks. What is the guardian's phone number?";
        }
        if (r.guardianPhone == null) {
            r.guardianPhone = text.trim();
            return "And the guardian's name?";
        }
        if (r.guardianName == null) {
            r.guardianName = text.trim();
            return "Summary: " + r.fullName + ", class " + r.classId + ", guardian "
                    + r.guardianName + " (" + r.guardianPhone + "). Confirm? Reply yes or no.";
        }
        if (text.trim().equalsIgnoreCase("yes")) {
            return finish(r, chatId);
        }
        r.markDone();
        return "Registration cancelled. Reply menu to see what I can do.";
    }

    private String finish(RegisterForm r, String chatId) {
        try {
            students.createStudent(r.fullName, r.classId, r.guardianPhone, r.guardianName);
            r.markDone();
            return "✅ Student registered!
" + r.fullName + " is in the system.
" + menuText();
        } catch (IllegalStateException e) {
            System.err.println("REGISTRATION FAILED for chat " + chatId + ": " + e.getMessage());
            r.markDone();
            return "Registration failed, please try again later.";
        }
    }

    /** The school menu, reused in the registration success message. */
    String menuText() {
        return "📚 *PocketSoftware School*

"
                + "1. Add student
"
                + "2. List students
"
                + "3. Find student
"
                + "4. Fees info

"
                + "Reply with a number, or type: add student, students, "
                + "find <name>, fees, or menu.";
    }
}
