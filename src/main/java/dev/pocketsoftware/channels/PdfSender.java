package dev.pocketsoftware.channels;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Sends PDF documents to Telegram chats. */
public final class PdfSender {

    private final TelegramBot bot;

    public PdfSender(TelegramBot bot) {
        this.bot = bot;
    }

    /** Sends one PDF built in memory; failures are logged and swallowed. */
    public void sendPdf(String chatId, String filename, byte[] pdf) {
        Path tmp = null;
        try {
            tmp = Files.createTempFile("report-", "-" + filename);
            Files.write(tmp, pdf);
            bot.execute(new SendDocument(chatId, tmp.toFile()).fileName(filename));
        } catch (IOException e) {
            System.err.println("PDF temp file failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Telegram PDF send failed: " + e.getMessage());
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            }
        }
    }
}
