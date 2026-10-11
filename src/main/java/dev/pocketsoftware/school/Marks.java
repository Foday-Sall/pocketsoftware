package dev.pocketsoftware.school;

/** Parses "add mark <student> <subject> <term> <score>" and saves it. */
public final class Marks {

    private Marks() {
    }

    /** Handles one add-mark command; returns the chat reply. */
    static String handleAddMark(GradeStore grades, String chatId, String text) {
        String body = text.trim().substring("add mark".length()).trim();
        String[] parts = body.split("\\s+");
        if (parts.length < 4) {
            return "Format: add mark <student> <subject> <term> <score>\n"
                    + "Example: add mark Fatou Math T1 85";
        }
        String student = parts[0];
        String subject = parts[1];
        String term = parts[2];
        int score;
        try {
            score = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return "Score must be a number, like: add mark Fatou Math T1 85";
        }
        if (score < 0 || score > 100) {
            return "Score should be 0-100.";
        }
        try {
            grades.addByName(student, subject, term, score);
            return "✅ Mark saved: " + student + " — " + subject + " " + term + " " + score + "/100.";
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }
}
