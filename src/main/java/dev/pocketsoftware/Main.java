package dev.pocketsoftware;

import dev.pocketsoftware.channels.TelegramChannel;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.KeywordClassifier;
import dev.pocketsoftware.engine.Router;

/** Loads config, wires Appwrite + Classifier + TelegramChannel, starts polling. */
public final class Main {

    public static void main(String[] args) {
        Config config = Config.load();
        System.setProperty("appwrite.project", config.get("appwrite.project"));
        System.setProperty("appwrite.key", config.get("appwrite.key"));
        new Appwrite(config); // validates endpoint config is present
        Router router = new Router(new KeywordClassifier(), new Appwrite(config), config, "telegram");
        String token = config.get("telegram.token");
        if (config.isPlaceholder("telegram.token")) {
            System.out.println("telegram.token is not configured — waiting for valid telegram token. "
                    + "Set telegram.token in config.properties and restart.");
            try {
                Thread.currentThread().join(); // park: systemd sees a healthy idle service, no crash loop
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }
        Thread bot = new Thread(new TelegramChannel(token, router), "telegram-poll");
        bot.start();
        System.out.println("PocketSoftware started (telegram long polling).");
    }

    private Main() {
    }
}
