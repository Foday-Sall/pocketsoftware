package dev.pocketsoftware.channels.gowa;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import dev.pocketsoftware.engine.Router;
import java.nio.charset.StandardCharsets;

/**
 * Turns one GOWA webhook body into a WhatsApp reply.
 *
 * Verifies the signature, filters to real incoming 1:1 text messages,
 * dedupes GOWA retries, then runs the Router round trip and sends the
 * reply. Runs after the HTTP 200 ACK is already on the wire, so slow
 * Appwrite calls never trigger a GOWA retry.
 */
public final class GowaChannel {

    private final Router router;
    private final GowaSender sender;
    private final GowaDedupe dedupe = new GowaDedupe();
    private final String webhookSecret;

    public GowaChannel(Router router, GowaSender sender, String webhookSecret) {
        this.router = router;
        this.sender = sender;
        this.webhookSecret = webhookSecret;
    }

    /** Handles one raw webhook body with its X-Hub-Signature-256 header. */
    public void onWebhook(byte[] body, String signature) {
        if (webhookSecret != null && !webhookSecret.isBlank()
                && !GowaSignature.isValid(body, signature, webhookSecret)) {
            System.err.println("GOWA webhook dropped: bad signature");
            return;
        }
        JsonObject root;
        try {
            root = Json.parse(new String(body, StandardCharsets.UTF_8)).asObject();
        } catch (Exception e) {
            System.err.println("GOWA webhook: unparseable body dropped");
            return;
        }
        if (!"message".equals(root.getString("event", ""))) {
            return; // acks, receipts, presence — not chat input
        }
        if (!root.get("payload").isObject()) {
            return;
        }
        JsonObject payload = root.get("payload").asObject();
        if (payload.getBoolean("is_from_me", false)) {
            return; // never answer my own outgoing messages
        }
        String text = payload.getString("body", "");
        if (text.isBlank()) {
            text = payload.getString("text", "");
        }
        String phone = GowaPhone.fromJid(payload.getString("from", ""));
        if (phone == null || text.isBlank()) {
            return; // groups, newsletters, media-only or malformed senders stay out
        }
        if (!dedupe.firstTime(payload.getString("id", ""))) {
            return; // re-sent webhook for a message we already handled
        }
        String reply = router.handle(phone, text);
        if (reply != null && !reply.isBlank()) {
            sender.sendText(phone, reply);
        }
    }
}
