package dev.pocketsoftware.school;

import java.time.LocalDate;

/** Parses "mark absent <student>" and "mark present <student>" for today. */
public final class MarkAttendance {

    private MarkAttendance() {
    }

    /** Handles one mark-attendance command; returns the chat reply. */
    static String handle(AttendanceStore attendance, String text) {
        String body = text.trim().substring("mark ".length()).trim();
        int space = body.indexOf(' ');
        if (space < 1) {
            return "Format: mark present <student> | mark absent <student> | mark late <student>";
        }
        String status = body.substring(0, space).toLowerCase();
        String student = body.substring(space + 1).trim();
        if (!status.equals("present") && !status.equals("absent") && !status.equals("late")) {
            return "Status must be present, absent or late.";
        }
        try {
            attendance.mark(student, LocalDate.now().toString(), status);
            return "✅ " + student + " marked " + status + " for today.";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }
}
