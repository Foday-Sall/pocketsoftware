package dev.pocketsoftware.engine;

/** Keyword-based fallback classifier. */
public final class KeywordClassifier implements Classifier {

    @Override
    public Decision classify(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        if (t.equals("menu") || t.equals("help") || t.equals("start")) {
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
        return new Decision(Actions.UNKNOWN, 0.2, null);
    }
}
