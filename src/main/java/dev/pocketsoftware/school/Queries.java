package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Decision;
import dev.pocketsoftware.school.model.Student;
import java.util.List;
import java.util.stream.Collectors;

/** Answers classified school commands and renders list texts. */
final class Queries {

    private Queries() {
    }

    /** Reply for a classified school command. */
    static String answer(StudentStore students, Decision d) {
        switch (d.action()) {
            case Actions.LIST_STUDENTS: return listText(students);
            case Actions.FIND_STUDENT: return find(students, d.slot());
            case Actions.FEES_INFO: return "Fees tracking coming soon.";
            default: return "Let's register a new student. First, what is the student's full name?";
        }
    }

    /** Names of all students, for chat. */
    static String listText(StudentStore students) {
        try {
            return "Students: " + String.join(", ", students.allNames());
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** Reply for the find command. */
    static String find(StudentStore students, String name) {
        try {
            List<Student> matches = students.search(name == null ? "" : name);
            if (matches.isEmpty()) {
                return "No student found for '" + name + "'.";
            }
            return "Found: " + matches.stream()
                    .map(Student::fullName).collect(Collectors.joining(", "));
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** All classes, for chat. */
    static String listClasses(ClassStore classes) {
        try {
            return "Classes: " + classes.all().stream()
                    .map(c -> c.name()).collect(Collectors.joining(", "));
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** All teachers, for chat. */
    static String listTeachers(StaffStore staff) {
        try {
            return "Teachers: " + staff.all().stream()
                    .map(s -> s.name()).collect(Collectors.joining(", "));
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** All courses, for chat. */
    static String listCourses(CourseStore courses) {
        try {
            return "Courses: " + courses.all().stream()
                    .map(c -> c.name()).collect(Collectors.joining(", "));
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }
}
