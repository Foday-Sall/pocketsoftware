package dev.pocketsoftware.school;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.school.model.Course;
import java.util.ArrayList;
import java.util.List;

/** Saves and lists courses (subjects). */
public final class CourseStore {

    private final Appwrite db;

    public CourseStore(Appwrite db) {
        this.db = db;
    }

    /** Adds a course and returns the stored record. */
    public Course add(String name, String code, String teacher) {
        Course c = new Course(null, name, code, teacher, "active");
        JsonObject row = db.createRow("school", "courses", c.data());
        return Course.from(row);
    }

    /** All courses. */
    public List<Course> all() {
        List<Course> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("school", "courses", null))) {
            out.add(Course.from(row));
        }
        return out;
    }
}
