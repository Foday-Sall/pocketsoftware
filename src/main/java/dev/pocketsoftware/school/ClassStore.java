package dev.pocketsoftware.school;

import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.school.model.SchoolClass;
import java.util.ArrayList;
import java.util.List;

/** Saves and lists school classes. */
public final class ClassStore {

    private final Appwrite db;

    public ClassStore(Appwrite db) {
        this.db = db;
    }

    /** Creates a class and returns the stored record. */
    public SchoolClass add(String name, String teacherPhone) {
        SchoolClass c = new SchoolClass(null, name, teacherPhone, "active");
        JsonObject row = db.createRow("school", "classes", c.data());
        return SchoolClass.from(row);
    }

    /** All classes. */
    public List<SchoolClass> all() {
        List<SchoolClass> out = new ArrayList<>();
        for (var row : db.rows(db.listRows("school", "classes", null))) {
            out.add(SchoolClass.from(row));
        }
        return out;
    }
}
