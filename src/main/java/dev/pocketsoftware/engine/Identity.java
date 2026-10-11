package dev.pocketsoftware.engine;

import dev.pocketsoftware.modules.Modules;
import com.eclipsesource.json.JsonObject;

/** Resolves the chat's school identity from the channel and chat id. */
final class Identity {

    private Identity() {
    }

    /** Looks up the student id bound to this chat; null when unknown or school absent. */
    static String studentId(Modules modules, String channel, String chatId) {
        try {
            dev.pocketsoftware.school.School school = schoolModule(modules);
            if (school == null) {
                return null;
            }
            JsonObject identity = school.students().identityByChannel(channel, chatId);
            return identity == null ? null : identity.getString("studentId", null);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static dev.pocketsoftware.school.School schoolModule(Modules modules) {
        Object m = modules.byId("school");
        return m instanceof dev.pocketsoftware.school.School school ? school : null;
    }
}
