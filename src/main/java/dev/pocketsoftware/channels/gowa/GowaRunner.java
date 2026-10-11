package dev.pocketsoftware.channels.gowa;

import dev.pocketsoftware.Config;
import dev.pocketsoftware.channels.WebhookServer;
import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.Router;
import dev.pocketsoftware.modules.Modules;

/**
 * Runs only the WhatsApp/GOWA channel — for container mode and testing.
 *
 * Independent of Main: builds Config, Appwrite, Router with channel
 * "whatsapp", then serves the webhook. No Telegram polling here.
 */
public final class GowaRunner {

    public static void main(String[] args) throws Exception {
        Config config = Config.load();
        System.setProperty("appwrite.project", config.get("appwrite.project"));
        System.setProperty("appwrite.key", config.get("appwrite.key"));
        Appwrite db = new Appwrite(config);
        Router router = new Router(Modules.load(config.get("active_modules")), db, "whatsapp");
        GowaChannel channel = new GowaChannel(router, sender(config), config.get("gowa.webhookSecret"));
        WebhookServer server = new WebhookServer(bindHost(config), port(config), "/gowa", channel::onWebhook);
        server.start();
        System.out.println("PocketSoftware GOWA channel started on " + bindHost(config) + ":" + port(config) + ".");
    }

    /** Sender wired from gowa.url/user/password/device; gowa.sendEnabled=false logs only. */
    private static GowaSender sender(Config config) {
        String enabled = config.get("gowa.sendEnabled");
        return new GowaSender(
                config.get("gowa.url"),
                config.get("gowa.user"),
                config.get("gowa.password"),
                config.get("gowa.device"),
                enabled.isBlank() || enabled.equals("true"));
    }

    /** Bind address: gowa.bind from config, loopback by default. */
    private static String bindHost(Config config) {
        String bind = config.get("gowa.bind");
        return bind.isBlank() ? "127.0.0.1" : bind;
    }

    /** Webhook listen port: gowa.port from config, default 8085. */
    private static int port(Config config) {
        String value = config.get("gowa.port");
        int port = value.isBlank() ? 8085 : Integer.parseInt(value);
        if (port <= 0 || port > 65535) {
            throw new IllegalStateException("gowa.port out of range: " + port);
        }
        return port;
    }
}
