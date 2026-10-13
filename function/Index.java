package io.openruntimes.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.models.InputFile;
import io.appwrite.models.Row;
import io.appwrite.models.RowList;
import io.appwrite.services.Functions;
import io.appwrite.services.Storage;
import io.appwrite.services.TablesDB;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PocketSoftware — one lean Appwrite Function that runs the whole product.
 *
 * Pipeline (one request = one message):
 *   1. CATCH    — normalize inbound text/sender/channel (generic, Meta,
 *                 Telegram and GOWA payload shapes), park attachments in Storage.
 *   2. DECIDE   — the decision model turns free text into one clean intent
 *                 token (choices come from the bot_actions table, so new
 *                 intents are data, not code). Keyword fallback when the
 *                 model is not configured.
 *   3. LOOKUP   — bot_actions maps the intent token to a target database,
 *                 collection and data field.
 *   4. FETCH+SEND — read the sender's row, merge the value into the tenant's
 *                 bot_templates body, deliver over the channel's REST API.
 *
 * Multi-step wizards live in platform.sessions; timed reminders are
 * scheduled with the platform's own execution queue (no cron code here).
 * Every user-visible sentence is rendered from bot_templates — nothing
 * hardcoded. All data is dynamic Map<String,Object> via the official SDK.
 */
public class Index {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    private static final String PLATFORM_DB = env("POCKET_PLATFORM_DB", "platform");
    private static final String ACTIONS = env("POCKET_ACTIONS_TABLE", "bot_actions");
    private static final String TEMPLATES = env("POCKET_TEMPLATES_TABLE", "bot_templates");
    private static final String SESSIONS = env("POCKET_SESSIONS_TABLE", "sessions");
    private static final Pattern REMINDER =
            Pattern.compile("(?i)remind me\\b.*?\\b(?:in|after) (\\d+) (minute|min|hour|hr|day)s?");

    /** Captured request headers; carries the injected ephemeral API key. */
    private static final ThreadLocal<Map<String, String>> REQ_HEADERS = new ThreadLocal<>();

    /** Clef's own confidence score for the current request, set by decide(). */
    private static final ThreadLocal<String> lastConfidence = ThreadLocal.withInitial(() -> "0.0");

    /** One SDK client per request: skips a fresh OkHttp instance per DB call. */
    private static final ThreadLocal<Client> REQ_CLIENT = new ThreadLocal<>();


    private static String reqHeader(String name) {
        Map<String, String> h = REQ_HEADERS.get();
        return h != null ? h.get(name.toLowerCase()) : null;
    }

    public RuntimeOutput main(RuntimeContext context) {
        long started = System.nanoTime();
        REQ_HEADERS.set(context.getReq().getHeaders());
        try {
            Map<String, Object> out = handle(context);
            out.put("ms", (System.nanoTime() - started) / 1_000_000);
            return context.getRes().json(out);
        } catch (Exception e) {
            context.error("pipeline failed: " + e);
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", String.valueOf(e.getMessage()));
            return context.getRes().json(err, 500);
        } finally {
            REQ_HEADERS.remove();
            lastConfidence.remove();
            REQ_CLIENT.remove();
        }
    }

    private Map<String, Object> handle(RuntimeContext context) throws Exception {
        Map<String, Object> body = readBody(context.getReq().getBody());
                ? "null" : context.getReq().getBody().getClass().getSimpleName());

        // ---- 1. CATCH: normalize sender/text/channel from any payload shape
        String text = str(firstNonNull(body.get("message_text"), body.get("text"),
                at(body, "entry", 0, "changes", 0, "value", "messages", 0, "text", "body"),
                at(body, "message", "text"),
                gowaText(body)));
        String sender = normalizeSender(
                str(firstNonNull(body.get("phone_number"), body.get("from"),
                        at(body, "entry", 0, "changes", 0, "value", "messages", 0, "from"),
                        at(body, "message", "chat", "id"),
                        at(body, "data", "key", "remoteJid"))),
                str(firstNonNull(body.get("channel_type"), body.get("channel"), guessChannel(body))));
        String channel = str(firstNonNull(body.get("channel_type"), body.get("channel"),
                guessChannel(body))).toUpperCase();
        String tenant = str(firstNonNull(body.get("tenant_id"), env("POCKET_TENANT_ID"), "main"));

        // A scheduled reminder came back to fire: deliver and stop.
        if (Boolean.TRUE.equals(body.get("reminder"))) {
            String reply = template(PLATFORM_DB, tenant, "reminder",
                    Map.of("text", str(body.get("message_text"))));
            send(channel, sender, reply);
            return done("reminder", "1.0", reply, null);
        }

        Map<String, Object> session = loadSession(tenant, sender);
        Map<String, Object> actions = loadActions();

        // Park any attachment in the Storage vault before answering.
        String attachmentId = stashAttachment(body);

        // ---- wizard in progress: the session state machine decides
        if (session != null) {
            String reply = stepSession(tenant, sender, channel, text, session, actions);
            send(channel, sender, reply);
            return done("wizard", "1.0", reply, attachmentId);
        }

        // ---- 2. DECIDE: intent token from Clef (confidence is Clef's own)
        List<String> choices = new ArrayList<>(actions.keySet());
        String intent = decide(text, choices, actions);
        String confidence = lastConfidence.get();

        // ---- reminder request: hand the countdown to the platform queue
        String scheduled = scheduleReminder(tenant, sender, channel, text, intent);
        if (scheduled != null) {
            String reply = template(PLATFORM_DB, tenant, "set_reminder", Map.of("time", scheduled));
            send(channel, sender, reply);
            return done("set_reminder", "1.0", reply, attachmentId);
        }

        // ---- 3. LOOKUP: intent -> target database / collection / field
        @SuppressWarnings("unchecked")
        Map<String, Object> action = (Map<String, Object>) actions.get(intent);

        // A multi-field action starts a wizard instead of reading a row.
        if (action != null && str(action.get("data_field_to_read")).contains(",")) {
            String reply = startWizard(tenant, sender, intent, action);
            send(channel, sender, reply);
            return done(intent, confidence, reply, attachmentId);
        }

        // ---- 4. FETCH+SEND: read the cell, merge into the template, deliver
        String reply;
        if (action == null) {
            reply = template(PLATFORM_DB, tenant, "unknown", Map.of("text", text));
        } else {
            String db = str(action.get("target_database_id"));
            if ("bot_templates".equals(str(action.get("target_collection")))) {
                // self-describing intent: template reply only, no caller row needed
                reply = template(PLATFORM_DB, tenant, intent, Map.of("text", text));
            } else {
                Map<String, Object> row = fetchRow(db, str(action.get("target_collection")), sender);
                if (row == null) {
                    reply = template(db, tenant, intent + "_missing",
                            Map.of("phone", sender, "name", sender));
                } else {
                    Map<String, Object> slots = new LinkedHashMap<>(row);
                    slots.put("value", row.get(str(action.get("data_field_to_read"))));
                    reply = template(db, tenant, intent, slots);
                }
            }
        }
        send(channel, sender, reply);
        return done(intent, confidence, reply, attachmentId);
    }

    // ------------------------------------------------------------------ steps

    /**
     * Decision chain: the primary model first, then the fallback router,
     * then keyword scoring. Confidence reflects the tier that answered.
     */
    private String decide(String text, List<String> choices, Map<String, Object> actions) {
        lastConfidence.set("0.0");
        String key = env("DECISION_API_KEY", "");
        String model = env("DECISION_MODEL", "clef-flash");
        String url = env("DECISION_URL", "");
        if (key.isEmpty() || url.isEmpty()) {
            lastConfidence.set("0.0");
            String fb = fallbackRouter(text, choices);
            if (fb != null) return fb;
            lastConfidence.set("0.2");
            return keyword(text, choices);
        }
        try {
            Map<String, Object> criteria = new LinkedHashMap<>();
            for (String c : choices) {
                criteria.put(c, "user wants to: " + c.replace('_', ' '));
            }
            criteria.put("unknown", "none of the other actions fit this message"); // escape hatch
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("state", text);
            payload.put("questions", Map.of("intent", Map.of(
                    "type", "choice",
                    "instructions", "Which action does the user's message request?",
                    "criteria", criteria)));
            String res = post(url, GSON.toJson(payload), Map.of(
                    "Authorization", "Bearer " + key,
                    "Content-Type", "application/json"));
            Map<String, Object> parsed = GSON.fromJson(res, Map.class);
            Map<String, Object> result = asMap(parsed.get("result"));
            Map<String, Object> answers = asMap(result.get("answers"));
            Map<String, Object> intentAnswer = asMap(answers.get("intent"));
            String choice = str(intentAnswer.get("choice"));
            lastConfidence.set(str(intentAnswer.get("confidence"))); // Clef's own score
            if (!choice.isEmpty() && actions.containsKey(choice)) {
                return choice;
            }
        } catch (Exception e) {
            // Clef unreachable or unparsable: fall through to the fallback chain
        }
        String fb = fallbackRouter(text, choices);
        if (fb != null) return fb;
        lastConfidence.set("0.2");
        return keyword(text, choices);
    }

    /**
     * Fallback router (master design): Cloudflare Workers AI tool-calling
     * classification via the OpenAI-compatible endpoint. Returns null when
     * unconfigured or unreachable so the caller drops to keyword matching.
     * Runs only after Clef fails - never replaces a healthy Clef.
     */
    private String fallbackRouter(String text, List<String> choices) {
        String token = env("CF_API_TOKEN", "");
        String account = env("CF_ACCOUNT_ID", "");
        if (token.isEmpty() || account.isEmpty()) {
            return null;
        }
        try {
            List<String> enumChoices = new ArrayList<>(choices);
            if (!enumChoices.contains("set_reminder")) {
                enumChoices.add("set_reminder");
            }
            if (!enumChoices.contains("unknown")) {
                enumChoices.add("unknown");
            }
            Map<String, Object> intentSchema = new LinkedHashMap<>();
            intentSchema.put("type", "string");
            intentSchema.put("enum", enumChoices);
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("type", "object");
            parameters.put("properties", Map.of("intent", intentSchema));
            parameters.put("required", List.of("intent"));
            Map<String, Object> tool = new LinkedHashMap<>();
            tool.put("type", "function");
            tool.put("function", Map.of(
                    "name", "classify_intent",
                    "description", "Classify the user's message into exactly one action from the enum",
                    "parameters", parameters));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", env("CF_DECISION_MODEL", "@cf/meta/llama-3.3-70b-instruct-fp8-fast"));
            payload.put("messages", List.of(
                    Map.of("role", "system",
                            "content", "Classify the user's message into exactly one action. Call the classify_intent tool with your single choice."),
                    Map.of("role", "user", "content", text)));
            payload.put("tools", List.of(tool));
            payload.put("tool_choice", Map.of("type", "function",
                    "function", Map.of("name", "classify_intent")));
            payload.put("temperature", 0);
            payload.put("max_tokens", 200);
            String res = post("https://api.cloudflare.com/client/v4/accounts/" + account
                    + "/ai/v1/chat/completions", GSON.toJson(payload), Map.of(
                    "Authorization", "Bearer " + token,
                    "Content-Type", "application/json"));
            Map<String, Object> parsed = GSON.fromJson(res, Map.class);
            if (!(parsed.get("choices") instanceof List)
                    || ((List<?>) parsed.get("choices")).isEmpty()) {
                return null;
            }
            Map<String, Object> message = asMap(
                    asMap(((List<?>) parsed.get("choices")).get(0)).get("message"));
            if (!(message.get("tool_calls") instanceof List)
                    || ((List<?>) message.get("tool_calls")).isEmpty()) {
                return null;
            }
            Map<String, Object> fn = asMap(
                    asMap(((List<?>) message.get("tool_calls")).get(0)).get("function"));
            Map<String, Object> args = asMap(GSON.fromJson(str(fn.get("arguments")), Map.class));
            String intent = str(args.get("intent"));
            if (!intent.isEmpty()) {
                lastConfidence.set("0.9");
            }
            return intent.isEmpty() ? null : intent;
        } catch (Exception e) {
            return null;
        }
    }

    /** Keyword scoring: the free last-resort classifier (master design degraded tier). */
    private static String keyword(String text, List<String> choices) {
        String clean = text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").trim();
        String best = "unknown";
        int bestScore = 0;
        for (String choice : choices) {
            String key = choice.toLowerCase().replace('_', ' ');
            int score = 0;
            for (String word : key.split(" ")) {
                if (!word.isEmpty() && clean.contains(word)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = choice;
            }
        }
        return best;
    }

    /** Returns an ISO timestamp when the message asks for a timed reminder. */
    private String scheduleReminder(String tenant, String sender, String channel,
                                    String text, String intent) {
        Matcher m = REMINDER.matcher(text);
        boolean found = m.find();
        if (!found) {
            return null; // no parseable time: fall through to normal lookup
        }
        long amount = Long.parseLong(m.group(1));
        long seconds = switch (m.group(2).toLowerCase()) {
            case "minute", "min" -> amount * 60;
            case "hour", "hr" -> amount * 3600;
            default -> amount * 86400;
        };
        long fireAt = ((Instant.now().getEpochSecond() + seconds + 59) / 60) * 60;
        String when = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(fireAt));
        String functionId = env("REMINDER_FUNCTION_ID", "");
        if (functionId.isEmpty()) {
            return when; // no self-reference configured; report time only
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("reminder", true);
            payload.put("message_text", text);
            // carry the requester so the fired execution can deliver to them
            payload.put("phone_number", sender);
            payload.put("channel_type", channel);
            payload.put("tenant_id", tenant);
            sync(cont -> new Functions(client()).createExecution(functionId,
                    GSON.toJson(payload), true, null, null, null, when, cont));
            return when;
        } catch (Exception e) {
            return null;
        }
    }

    /** Starts a wizard: parks the field list in the session row. */
    private String startWizard(String tenant, String sender, String key,
                               Map<String, Object> action) {
        String db = str(action.get("target_database_id"));
        List<String> fields = List.of(str(action.get("data_field_to_read")).split(","));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fields", fields);
        data.put("index", 0);
        data.put("values", new LinkedHashMap<String, Object>());
        saveSession(tenant, sender, key, data);
        return template(db, tenant, key + "_ask_" + fields.get(0),
                Map.of("field", fields.get(0)));
    }

    /** Advances (or confirms/cancels) the wizard and returns the next reply. */
    private String stepSession(String tenant, String sender, String channel, String text,
                               Map<String, Object> session, Map<String, Object> actions) {
        String key = str(session.get("current_step"));
        @SuppressWarnings("unchecked")
        Map<String, Object> action = (Map<String, Object>) actions.get(key);
        String db = action != null ? str(action.get("target_database_id")) : PLATFORM_DB;
        Map<String, Object> data = asMap(parse(str(session.get("state"))));
        List<String> fields = listOf(data.get("fields"));
        int index = (int) toDouble(data.get("index"));
        Map<String, Object> values = asMap(data.get("values"));

        if (text.matches("(?i)^(no|n|cancel|stop)$")) {
            deleteSession(tenant, sender);
            return template(db, tenant, key + "_cancelled", Map.of());
        }
        if (text.matches("(?i)^(yes|y|confirm)$") && index >= fields.size() && action != null) {
            values.put("phone", sender); // the phone number is the identity
            try {
                sync(cont -> tables().createRow(str(action.get("target_database_id")),
                        str(action.get("target_collection")),
                        str(ID.Companion.unique(8)), values, null, null, mapClass(), cont));
            } catch (Exception e) {
                return template(db, tenant, key + "_cancelled", Map.of());
            }
            deleteSession(tenant, sender);
            return template(db, tenant, key + "_saved",
                    Map.of("summary", summary(fields, values)));
        }
        if (text.isBlank() || index >= fields.size()) {
            return template(db, tenant, key + "_confirm",
                    Map.of("summary", summary(fields, values)));
        }
        values.put(fields.get(index), text);
        data.put("values", values);
        data.put("index", index + 1);
        saveSession(tenant, sender, key, data);
        if (index + 1 < fields.size()) {
            return template(db, tenant, key + "_ask_" + fields.get(index + 1),
                    Map.of("field", fields.get(index + 1)));
        }
        return template(db, tenant, key + "_confirm", Map.of("summary", summary(fields, values)));
    }

    // ----------------------------------------------------------------- data

    /** Lists rows: the single SDK query wrapper every lookup goes through. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rows(String db, String table, List<String> queries)
            throws Exception {
        RowList<Map<String, Object>> result = rowList(sync(cont -> tables().listRows(db, table,
                queries, null, null, null, mapClass(), cont)));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Row<Map<String, Object>> r : result.getRows()) {
            out.add(r.getData());
        }
        return out;
    }

    /** The tenant+user session lookup, shared by load/save/delete. */
    private List<Map<String, Object>> findSession(String tenant, String sender)
            throws Exception {
        List<String> queries = new ArrayList<>();
        queries.add(Query.Companion.equal("tenant_id", tenant));
        queries.add(Query.Companion.equal("user_id", sender));
        queries.add(Query.Companion.limit(1));
        return rows(PLATFORM_DB, SESSIONS, queries);
    }

    private Map<String, Object> fetchRow(String db, String table, String sender)
            throws Exception {
        if (db.isEmpty() || table.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> found = rows(db, table,
                List.of(Query.Companion.equal("phone", sender), Query.Companion.limit(1)));
        return found.isEmpty() ? null : found.get(0);
    }

    private Map<String, Object> loadActions() {
        try {
            List<Map<String, Object>> found = rows(PLATFORM_DB, ACTIONS,
                    List.of(Query.Companion.limit(100)));
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map<String, Object> row : found) {
                map.put(str(row.get("template_key")), row);
            }
            return map;
        } catch (Exception e) {
            return new LinkedHashMap<>(); // no registry yet: everything is unknown
        }
    }

    private Map<String, Object> loadSession(String tenant, String sender) {
        try {
            List<Map<String, Object>> found = findSession(tenant, sender);
            return found.isEmpty() ? null : found.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveSession(String tenant, String sender, String key,
                             Map<String, Object> data) {
        try {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tenant_id", tenant);
            row.put("user_id", sender);
            row.put("current_step", key);
            row.put("state", GSON.toJson(data));
            List<Map<String, Object>> existing = findSession(tenant, sender);
            if (!existing.isEmpty()) {
                String id = str(existing.get(0).get("$id"));
                sync(cont -> tables().updateRow(PLATFORM_DB, SESSIONS,
                        id, row, null, null, mapClass(), cont));
            } else {
                sync(cont -> tables().createRow(PLATFORM_DB, SESSIONS,
                        sanitize(tenant + "_" + sender), row, null, null, mapClass(), cont));
            }
        } catch (Exception ignored) {
            // sessions are an optimization; the pipeline must not die on them
        }
    }

    private void deleteSession(String tenant, String sender) {
        try {
            List<Map<String, Object>> existing = findSession(tenant, sender);
            if (!existing.isEmpty()) {
                String id = str(existing.get(0).get("$id"));
                sync(cont -> tables().deleteRow(PLATFORM_DB, SESSIONS, id, cont));
            }
        } catch (Exception ignored) {
        }
    }

    /** Renders the tenant's template row, substituting {slot} placeholders. */
    private String template(String db, String tenant, String key, Map<String, Object> slots) {
        String body = "";
        try {
            List<Map<String, Object>> found = rows(db, TEMPLATES,
                    List.of(Query.Companion.equal("tenant_id", tenant),
                            Query.Companion.equal("template_key", key),
                            Query.Companion.limit(1)));
            if (!found.isEmpty()) {
                body = str(found.get(0).get("body"));
            } else if (!PLATFORM_DB.equals(db)) {
                return template(PLATFORM_DB, tenant, key, slots); // platform holds the defaults
            } else {
                body = fallback(key, slots); // minimal net; templates should exist
            }
        } catch (Exception e) {
            body = fallback(key, slots);
        }
        String out = body;
        for (Map.Entry<String, Object> e : slots.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", str(e.getValue()));
        }
        return out;
    }

    /** Safety net when a template row is missing (templates should cover these). */
    private String fallback(String key, Map<String, Object> slots) {
        if (key.startsWith("reminder")) {
            return "Reminder: " + str(slots.get("text"));
        }
        if (key.contains("_ask_")) {
            return "Please send " + str(slots.get("field")) + ":";
        }
        if (key.contains("_confirm")) {
            return "Confirm? YES / NO\n" + str(slots.get("summary"));
        }
        if (key.contains("_saved")) {
            return "Saved.";
        }
        if (key.contains("_cancelled")) {
            return "Cancelled.";
        }
        if (key.contains("_missing")) {
            return "No record found for this number.";
        }
        if (key.contains("set_reminder")) {
            return "Reminder set for " + str(slots.get("time"));
        }
        return "Unknown request.";
    }

    /** Downloads an attachment link and parks the bytes in Storage. */
    private String stashAttachment(Map<String, Object> body) {
        String url = str(firstNonNull(body.get("attachment_url"), body.get("file_url")));
        String bucket = env("POCKET_ATTACHMENT_BUCKET", "");
        if (!isFetchableUrl(url) || bucket.isEmpty()) {
            return null;
        }
        try {
            HttpResponse<byte[]> res = HTTP.send(
                    HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            String name = URLDecoder.decode(
                    URI.create(url).getPath().replaceAll(".*/", ""), StandardCharsets.UTF_8);
            String mime = res.headers().firstValue("content-type").orElse("application/octet-stream");
            Object file = sync(cont -> new Storage(client()).createFile(bucket,
                    str(ID.Companion.unique(8)), InputFile.Companion.fromBytes(res.body(), name, mime),
                    null, cont));
            return str(asMap(file).get("$id"));
        } catch (Exception e) {
            return null;
        }
    }

    /** Only https URLs on public hosts; blocks redirects to anything else. */
    private static boolean isFetchableUrl(String url) {
        if (url == null) {
            return false;
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            return false;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            return false;
        }
        return !resolvesToPrivateHost(uri.getHost());
    }

    private static boolean resolvesToPrivateHost(String host) {
        try {
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()
                        || addr.isMulticastAddress()) {
                    return true;
                }
                byte[] b = addr.getAddress();
                int octet0 = b[0] & 0xFF;
                int octet1 = b[1] & 0xFF;
                if (octet0 == 100 && (octet1 & 0xC0) == 128) { // 100.64.0.0/10 CGNAT
                    return true;
                }
                if (octet0 == 192 && octet1 == 0 && (b[2] & 0xFF) == 2) { // 192.0.2.0/24 TEST-NET
                    return true;
                }
                if (octet0 == 198 && (octet1 == 18 || octet1 == 19)) { // 198.18.0.0/15 benchmark
                    return true;
                }
                if (b.length == 16 && (b[0] & 0xFF) == 0xfd) { // unique local IPv6 fc00::/7
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return true; // unresolvable host: treat as private (fail closed)
        }
    }

    /** Delivers the reply over the channel's own REST API. */
    private void send(String channel, String sender, String reply) {
        try {
            if ("WHATSAPP".equals(channel)) {
                String version = env("WHATSAPP_API_VERSION", "v21.0");
                String phoneId = env("WHATSAPP_PHONE_NUMBER_ID", "");
                String token = env("WHATSAPP_TOKEN", "");
                if (phoneId.isEmpty() || token.isEmpty()) {
                    return;
                }
                post("https://graph.facebook.com/" + version + "/" + phoneId + "/messages",
                        GSON.toJson(Map.of("messaging_product", "whatsapp", "to", sender,
                                "type", "text",
                                "text", Map.of("preview_url", false, "body", reply))),
                        Map.of("Authorization", "Bearer " + token,
                                "Content-Type", "application/json"));
            } else if ("TELEGRAM".equals(channel)) {
                String token = env("TELEGRAM_BOT_TOKEN", "");
                if (token.isEmpty()) {
                    return;
                }
                post("https://api.telegram.org/bot" + token + "/sendMessage",
                        GSON.toJson(Map.of("chat_id", sender, "text", reply)),
                        Map.of("Content-Type", "application/json"));
            }
        } catch (Exception ignored) {
            // delivery problems must not fail the pipeline response
        }
    }

    // -------------------------------------------------------------- plumbing

    private static Object sync(SdkCall call) throws Exception {
        AtomicReference<Object> out = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        CoroutineCallback<Object> continuation = new CoroutineCallback<>((result, error) -> {
            out.set(error != null ? error : result);
            done.countDown();
        });
        call.invoke(continuation);
        if (!done.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("call timed out after 30s");
        }
        Object value = out.get();
        if (value instanceof Exception e) {
            throw e;
        }
        if (value instanceof Throwable t) {
            throw new IllegalStateException(t);
        }
        return value;
    }

    private interface SdkCall {
        Object invoke(CoroutineCallback<Object> continuation) throws Exception;
    }

    private static TablesDB tables() {
        return new TablesDB(client());
    }

    private static Client client() {
        Client cached = REQ_CLIENT.get();
        if (cached != null) {
            return cached;
        }
        Client c = new Client();
        c.setEndpoint(firstNonBlank(env("APPWRITE_FUNCTION_API_ENDPOINT", ""), "http://localhost/v1"));
        c.setProject(firstNonBlank(
                firstNonBlank(env("POCKET_TENANT_PROJECT", ""), env("APPWRITE_FUNCTION_PROJECT_ID", "")),
                "main"));
        c.setKey(firstNonBlank(
                firstNonBlank(env("POCKET_TENANT_KEY"), reqHeader("x-appwrite-key")),
                env("APPWRITE_FUNCTION_API_KEY")));
        REQ_CLIENT.set(c);
        return c;
    }

    private static String post(String url, String json, Map<String, String> headers)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        headers.forEach(builder::header);
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    private static Map<String, Object> readBody(Object raw) {
        if (raw instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        try {
            String text = raw instanceof String s ? s : String.valueOf(raw);
            if (text.isBlank() || !text.strip().startsWith("{")) {
                return new LinkedHashMap<>();
            }
            return new LinkedHashMap<>(GSON.fromJson(text, Map.class));
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private static String guessChannel(Map<String, Object> body) {
        if (body.containsKey("entry") || body.containsKey("data")) {
            return "WHATSAPP";
        }
        if (body.containsKey("message")) {
            return "TELEGRAM";
        }
        return env("POCKET_DEFAULT_CHANNEL", "TELEGRAM");
    }

    private static String normalizeSender(String sender, String channel) {
        if (sender == null || sender.isEmpty()) {
            return "unknown";
        }
        String s = sender.replace("@s.whatsapp.net", "").replace("@lid", "");
        return s.replaceAll("[^0-9]", ""); // digits-only: Meta and Telegram IDs are both numeric
    }

    private static String gowaText(Map<String, Object> body) {
        Object t = at(body, "data", "message", "conversation");
        if (t == null) {
            t = at(body, "data", "message", "extendedTextMessage", "text");
        }
        return t == null ? null : String.valueOf(t);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map<?, ?> m ? new LinkedHashMap<>((Map<String, Object>) m)
                : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static List<String> listOf(Object o) {
        List<String> out = new ArrayList<>();
        if (o instanceof List<?> l) {
            for (Object item : l) {
                out.add(str(item));
            }
        }
        return out;
    }

    private static Object at(Object root, Object... path) {
        Object cur = root;
        for (Object p : path) {
            if (cur instanceof Map<?, ?> m) {
                cur = m.get(p);
            } else if (cur instanceof List<?> l && p instanceof Integer i && i < l.size()) {
                cur = l.get(i);
            } else {
                return null;
            }
            if (cur == null) {
                return null;
            }
        }
        return cur;
    }

    private static String summary(List<String> fields, Map<String, Object> values) {
        List<String> lines = new ArrayList<>();
        for (String f : fields) {
            lines.add(f + ": " + str(values.get(f)));
        }
        return String.join("\n", lines);
    }

    private static Object parse(String json) {
        try {
            return GSON.fromJson(json, Map.class);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private static String sanitize(String id) {
        return id.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Object firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null && !String.valueOf(v).isEmpty()) {
                return v;
            }
        }
        return null;
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    @SuppressWarnings("unchecked")
    private static RowList<Map<String, Object>> rowList(Object value) {
        return (RowList<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static Class<Map<String, Object>> mapClass() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0;
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return v == null || v.isEmpty() ? fallback : v;
    }

    private static String env(String key) {
        return env(key, "");
    }

    private static Map<String, Object> done(String intent, String confidence, String reply,
                                            String attachmentId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reply", reply);
        out.put("intent", intent);
        out.put("confidence", confidence);
        if (attachmentId != null) {
            out.put("attachment_id", attachmentId);
        }
        return out;
    }
}
