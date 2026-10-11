package dev.pocketsoftware;

import dev.pocketsoftware.channels.TelegramChannel;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Router;
import dev.pocketsoftware.modules.Modules;

/** Loads config, wires the active modules into the router, starts Telegram polling. */
public final class Main {

    public static void main(String[] args) {
        dev.pocketsoftware.setup.FirstRun.maybeRun(); // runs wizard until setup.completed=true
        Config config = Config.load();
        System.setProperty("appwrite.project", config.get("appwrite.project"));
        System.setProperty("appwrite.key", config.get("appwrite.key"));
        new Appwrite(config); // validates endpoint config is present
        Router router = new Router(Modules.load(config.get("active_modules")),
                new Appwrite(config), "telegram");
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
