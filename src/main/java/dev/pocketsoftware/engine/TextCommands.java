package dev.pocketsoftware.engine;

/** Recognizes message shapes shared by all modules: greetings and menu digits. */
final class TextCommands {

    private TextCommands() {
    }

    /** True for plain greetings ("hi", "hello", Telegram "/start@BotName"). */
    static boolean isGreeting(String text) {
        String t = normalize(text);
        return t.equals("start") || t.equals("hi") || t.equals("hello") || t.equals("hey");
    }

    /** The menu digit 1-9 a message consists of, or 0 when it is not a bare digit. */
    static int asDigit(String text) {
        String t = text == null ? "" : text.trim();
        return t.length() == 1 && t.charAt(0) >= '1' && t.charAt(0) <= '9'
                ? t.charAt(0) - '0' : 0;
    }

    /** Lowercased text with Telegram command prefixes removed. */
    static String normalize(String text) {
        String t = text == null ? "" : text.trim().toLowerCase();
        if (t.startsWith("/")) {
            t = t.substring(1);
        }
        int at = t.indexOf('@');
        if (at > 0) {
            t = t.substring(0, at);
        }
        return t;
    }
}
