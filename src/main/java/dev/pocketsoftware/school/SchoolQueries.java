package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Decision;


/** Answers classified school commands: list, find, fees. */
final class SchoolQueries {

    private final Student students;

    SchoolQueries(Student students) {
        this.students = students;
    }

    /** Reply for a classified school command. */
    String answer(Decision d) {
        switch (d.action()) {
            case Actions.LIST_STUDENTS: return list();
            case Actions.FIND_STUDENT: return find(d.slot());
            case Actions.FEES_INFO: return fees();
            default: return "Let's register a new student. First, what is the student's full name?";
        }
    }

    /** Reply for the list menu entry. */
    String list() {
        try {
            return "Students: " + String.join(", ", students.allNames());
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** Reply for the find command. */
    String find(String name) {
        try {
            var matches = students.searchNames(name == null ? "" : name);
            return matches.isEmpty()
                    ? "No student found for '" + name + "'."
                    : "Found: " + String.join(", ", matches);
        } catch (IllegalStateException e) {
            return "Could not reach the database right now.";
        }
    }

    /** Reply for the fees entry. */
    String fees() {
        return "Fees tracking coming soon.";
    }
}
