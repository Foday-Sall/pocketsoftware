package dev.pocketsoftware.engine;

/** How modules deliver messages and documents to a chat. */
public interface MessageSender {

    /** Sends one text message. */
    void send(String chatId, String text);

    /** Sends one PDF document; default no-op for text-only senders. */
    default void sendPdf(String chatId, String filename, byte[] pdf) {
    }
}
