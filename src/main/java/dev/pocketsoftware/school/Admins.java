package dev.pocketsoftware.school;

/** Parses the add class/teacher/course commands and saves them. */
public final class Admins {

    private Admins() {
    }

    /** add class <name> <teacherPhone> */
    static String handleAddClass(ClassStore classes, String text) {
        String[] parts = text.trim().substring("add class".length()).trim().split("\\s+");
        if (parts.length < 1 || parts[0].isBlank()) {
            return "Format: add class <name> <teacherPhone>\nExample: add class G5A 2207000001";
        }
        String phone = parts.length > 1 ? parts[1] : "";
        try {
            classes.add(parts[0], phone);
            return "✅ Class " + parts[0] + " added.";
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** add teacher <name> <phone> */
    static String handleAddTeacher(StaffStore staff, String text) {
        String[] parts = text.trim().substring("add teacher".length()).trim().split("\\s+");
        if (parts.length < 1 || parts[0].isBlank()) {
            return "Format: add teacher <name> <phone>\nExample: add teacher Mr. Sallah 2207000001";
        }
        String phone = parts.length > 1 ? parts[1] : "";
        try {
            staff.add(parts[0], phone, "teacher");
            return "✅ Teacher " + parts[0] + " added.";
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** add course <name> <code> */
    static String handleAddCourse(CourseStore courses, String text) {
        String[] parts = text.trim().substring("add course".length()).trim().split("\\s+");
        if (parts.length < 1 || parts[0].isBlank()) {
            return "Format: add course <name> <code>\nExample: add course Mathematics MATH";
        }
        String code = parts.length > 1 ? parts[1] : "";
        try {
            courses.add(parts[0], code, "");
            return "✅ Course " + parts[0] + " added.";
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }
}
