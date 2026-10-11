package dev.pocketsoftware.channels;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import dev.pocketsoftware.engine.Router;

/** Telegram long-polling channel (no inbound port needed). */
public final class TelegramChannel implements Runnable {

    private final TelegramBot bot;
    private final Router router;

    public TelegramChannel(String token, Router router) {
        this.bot = new TelegramBot(token);
        this.router = router;
    }

    @Override
    public void run() {
        int offset = 0;
        while (true) {
            try {
                for (Update update : bot.execute(new GetUpdates().offset(offset).timeout(30)).updates()) {
                    offset = update.updateId() + 1;
                    Message message = update.message();
                    if (message == null || message.text() == null) {
                        continue;
                    }
                    String chatId = String.valueOf(message.chat().id());
                    String reply = router.handle(chatId, message.text());
                    send(chatId, reply);
                }
            } catch (Exception e) {
                sleep();
            }
        }
    }

    /** Sends one message; failures are logged and swallowed. */
    public void send(String chatId, String text) {
        try {
            bot.execute(new SendMessage(chatId, text));
        } catch (Exception e) {
            System.err.println("Telegram send failed: " + e.getMessage());
        }
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
