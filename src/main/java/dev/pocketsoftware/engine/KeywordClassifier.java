package dev.pocketsoftware.engine;

/** Keyword-based fallback classifier. */
public final class KeywordClassifier implements Classifier {

    @Override
    public Decision classify(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        // Normalize Telegram commands: "/start@BotName" -> "start"
        if (t.startsWith("/")) {
            t = t.substring(1);
        }
        int at = t.indexOf('@');
        if (at > 0) {
            t = t.substring(0, at);
        }
        if (t.equals("menu") || t.equals("help") || t.equals("start")
                || t.equals("hi") || t.equals("hello") || t.equals("hey")) {
            return new Decision(Actions.MENU, 0.9, null);
        }
        if (t.contains("add student") || t.contains("register new student") || t.equals("register")) {
            return new Decision(Actions.REGISTER_STUDENT, 0.9, null);
        }
        if (t.equals("students") || t.contains("list students")) {
            return new Decision(Actions.LIST_STUDENTS, 0.9, null);
        }
        if (t.startsWith("find ")) {
            return new Decision(Actions.FIND_STUDENT, 0.8, text.trim().substring(5).trim());
        }
        if (t.contains("fees") || t.contains("balance")) {
            return new Decision(Actions.FEES_INFO, 0.7, null);
        }
        if (t.startsWith("add mark")) {
            return new Decision(Actions.ADD_MARK, 0.8, null);
        }
        if (t.startsWith("report") || t.contains("send me report")) {
            return new Decision(Actions.REPORT, 0.8, null);
        }
        if (t.startsWith("add class")) {
            return new Decision(Actions.ADD_CLASS, 0.8, null);
        }
        if (t.startsWith("add teacher")) {
            return new Decision(Actions.ADD_TEACHER, 0.8, null);
        }
        if (t.startsWith("add course")) {
            return new Decision(Actions.ADD_COURSE, 0.8, null);
        }
        return new Decision(Actions.UNKNOWN, 0.2, null);
    }
}
