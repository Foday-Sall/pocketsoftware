package dev.pocketsoftware.channels.gowa;

/** Normalizes WhatsApp sender JIDs to the bare phone number used as identity. */
public final class GowaPhone {

    private GowaPhone() {
    }

    /**
     * "4917612345678@s.whatsapp.net" -> "4917612345678".
     * Group, newsletter, LID and malformed JIDs return null: identity must stay a phone.
     */
    public static String fromJid(String jid) {
        if (jid == null || !jid.endsWith("@s.whatsapp.net")) {
            return null;
        }
        String digits = jid.substring(0, jid.length() - "@s.whatsapp.net".length());
        return digits.matches("\\d+") ? digits : null;
    }
}
