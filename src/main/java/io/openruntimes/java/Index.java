package io.openruntimes.java;

import dev.pocketsoftware.engine.Actions;
import dev.pocketsoftware.engine.Decision;
import dev.pocketsoftware.engine.ModelClassifier;
import io.appwrite.Client;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.enums.MessagingProviderType;
import io.appwrite.models.Execution;
import io.appwrite.models.Row;
import io.appwrite.models.RowList;
import io.appwrite.models.Target;
import io.appwrite.models.User;
import io.appwrite.models.UserList;
import io.appwrite.services.Functions;
import io.appwrite.services.Messaging;
import io.appwrite.services.TablesDB;
import io.appwrite.services.Users;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unified inbound-message orchestrator ("traffic controller") for the
 * multi-tenant bot, packaged as an Appshine Function for the Java runtime
 * (entrypoint Index.java, runtime java-17.0+).
 *
 * One request = one pipeline run:
 *   1. capture the message (text, sender, tenant),
 *   2. the decision model picks the intent (keyword fallback only),
 *   3. run the intent's data action against the tenant's own database,
 *   4. track multi-step flows in the platform "sessions" table,
 *   5. render the reply from the tenant's row in "bot_templates" -
 *      no message text is hardcoded in this file.
 *
 * All Appwrite access goes through the official Kotlin SDK driven from
 * plain Java: every suspend call receives the SDK's own CoroutineCallback
 * as its continuation (see sync()). No raw HTTP clients touch Appwrite.
 */
public class Index {

    private static final String PLATFORM_DB = "platform";
    private static final String TEMPLATES = "bot_templates";
    private static final String SESSIONS = "sessions";

    /** Entry point invoked by the Open Runtimes executor. */
    public static RuntimeOutput main(RuntimeContext context) {
        bindHeaders(context.getReq().getHeaders());
        try {
            return handle(context);
        } catch (Exception e) {
            context.error("orchestrator failed: " + e);
            return context.getRes().json(Map.<String, Object>of(
                    "error", String.valueOf(e.getMessage())), 500);
        }
    }

    private static RuntimeOutput handle(RuntimeContext context) {
        // 1. Capture: tenant, sender and text from the incoming webhook.
        String tenant = firstNonBlank(env("POCKET_TENANT_ID"), "main");
        String sender = firstNonBlank(context.getReq().getHeaders().get("x-sender-id"), "unknown");
        String text = textOf(context.getReq());

        context.log("in: tenant=" + tenant + " sender=" + sender + " text=" + text);

        // 2. Classify: the decision model picks the intent; keywords only
        //    kick in when the model is not configured for this deployment.
        Decision decision = new ModelClassifier(
                env("discover.url"), env("discover.model"), env("discover.apiKey"))
                .classify(text);
        context.log("intent=" + decision.action() + " confidence=" + decision.confidence());

        // 3. Execute the intent's data action against the tenant database.
        Map<String, Object> slots = new LinkedHashMap<>();
        if (isDataAction(decision.action())) {
            executeAction(decision.action(), text, tenant, slots, context);
        }

        // 4. Multi-step flows live in the sessions table (current_step).
        if (isFlow(decision.action())) {
            saveSession(tenant, sender, decision.action(), context);
        }

        // 5. Reply: render the tenant's stored template for this intent.
        String reply = render(loadTemplate(tenant, decision.action(), context), slots);
        return context.getRes().json(Map.<String, Object>of(
                "reply", reply,
                "intent", decision.action(),
                "confidence", String.valueOf(decision.confidence())));
    }

    /** Reads the inbound payload; webhooks may arrive as JSON or plain text. */
    private static String textOf(RuntimeRequest req) {
        try {
            String raw = req.getBodyText();
            if (raw == null || raw.isBlank()) {
                return "";
            }
            raw = raw.trim();
            if (!raw.startsWith("{")) {
                return raw;
            }
            Map<String, Object> body = req.getBodyJson();
            if (body == null) {
                return "";
            }
            for (String key : List.of("text", "message")) {
                Object v = body.get(key);
                if (v instanceof String s && !s.isBlank()) {
                    return s;
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    /** Loads the tenant's template row for this intent; falls back to UNKNOWN. */
    private static Row<Map<String, Object>> loadTemplate(String tenant, String key,
                                                         RuntimeContext context) {
        try {
            List<String> queries = List.of(
                    Query.Companion.equal("tenant_id", tenant),
                    Query.Companion.equal("template_key", key),
                    Query.Companion.limit(1));
            RowList<Map<String, Object>> found = rowList(sync(cont ->
                    tables().listRows(PLATFORM_DB, TEMPLATES, queries, null, null, null,
                            mapClass(), cont)));
            if (found.getTotal() > 0) {
                return found.getRows().get(0);
            }
            return Actions.UNKNOWN.equals(key) ? null : loadTemplate(tenant, Actions.UNKNOWN, context);
        } catch (Exception e) {
            context.error("template lookup failed: " + e);
            return null;
        }
    }

    /** Intents that touch tenant data instead of just answering. */
    private static boolean isDataAction(String intent) {
        return switch (intent) {
            case Actions.REGISTER_STUDENT, Actions.REGISTER_PATIENT, Actions.ADD_CUSTOMER,
                    Actions.FIND_STUDENT, Actions.LIST_STUDENTS, Actions.RECORD_SALE,
                    Actions.BOOK_APPOINTMENT, Actions.ADD_MARK -> true;
            default -> false;
        };
    }

    /** Intents that start or continue a multi-step wizard. */
    private static boolean isFlow(String intent) {
        return switch (intent) {
            case Actions.REGISTER_STUDENT, Actions.REGISTER_PATIENT, Actions.ADD_CUSTOMER,
                    Actions.BOOK_APPOINTMENT -> true;
            default -> false;
        };
    }

    /** Runs one business action; fills the template slots it produces. */
    private static void executeAction(String intent, String text, String tenant,
                                      Map<String, Object> slots, RuntimeContext context) {
        try {
            switch (intent) {
                case Actions.FIND_STUDENT -> {
                    String term = text.length() > 5 ? text.substring(5).trim() : text.trim();
                    List<String> q = List.of(
                            Query.Companion.search("fullName", term),
                            Query.Companion.limit(1));
                    RowList<Map<String, Object>> rows = rowList(sync(cont ->
                            tables().listRows(tenant, "students", q, null, null, null,
                                    mapClass(), cont)));
                    if (rows.getTotal() > 0) {
                        Map<String, Object> s = rows.getRows().get(0).getData();
                        slots.put("name", s.getOrDefault("fullName", ""));
                        slots.put("class", s.getOrDefault("classId", "-"));
                        slots.put("admission", s.getOrDefault("admissionNo", "-"));
                    } else {
                        slots.put("name", term);
                        slots.put("class", "-");
                    }
                }
                case Actions.LIST_STUDENTS -> {
                    RowList<Map<String, Object>> rows = rowList(sync(cont ->
                            tables().listRows(tenant, "students",
                                    List.of(Query.Companion.limit(5)), null, null, null,
                                    mapClass(), cont)));
                    List<String> names = new ArrayList<>();
                    for (Row<Map<String, Object>> r : rows.getRows()) {
                        names.add(String.valueOf(r.getData().get("fullName")));
                    }
                    slots.put("count", String.valueOf(rows.getTotal()));
                    slots.put("names", String.join(", ", names));
                }
                default -> slots.put("name", text.trim());
            }
        } catch (Exception e) {
            context.error("action " + intent + " failed: " + e);
            slots.put("error", String.valueOf(e.getMessage()));
        }
    }

    /** Creates or updates the conversation session row for this sender. */
    private static void saveSession(String tenant, String user, String step,
                                    RuntimeContext context) {
        try {
            List<String> q = List.of(
                    Query.Companion.equal("tenant_id", tenant),
                    Query.Companion.equal("user_id", user),
                    Query.Companion.limit(1));
            RowList<Map<String, Object>> rows = rowList(sync(cont ->
                    tables().listRows(PLATFORM_DB, SESSIONS, q, null, null, null,
                            mapClass(), cont)));
            Map<String, Object> data = new HashMap<>();
            data.put("tenant_id", tenant);
            data.put("user_id", user);
            data.put("current_step", step);
            if (rows.getTotal() > 0) {
                String id = rows.getRows().get(0).getId();
                sync(cont -> tables().updateRow(PLATFORM_DB, SESSIONS, id, data,
                        null, null, mapClass(), cont));
            } else {
                sync(cont -> tables().createRow(PLATFORM_DB, SESSIONS, tenant + "_" + user,
                        data, null, null, mapClass(), cont));
            }
        } catch (Exception e) {
            context.error("session save failed: " + e);
        }
    }

    /**
     * Schedules a delayed execution on a worker function. The delay is
     * expressed with scheduledAt (ISO 8601, minute precision) - the timing
     * parameter the platform provides; Appwrite runs the countdown in its
     * own queue and fires the worker when it hits zero. Rounded up to the
     * next minute so the wait is never shorter than requested.
     */
    public static String scheduleReminder(String functionId, Map<String, Object> payload,
                                          long delaySeconds, RuntimeContext context) {
        try {
            String scheduledAt = OffsetDateTime.now(ZoneOffset.UTC)
                    .plusSeconds(delaySeconds)
                    .truncatedTo(ChronoUnit.MINUTES)
                    .plusMinutes(1)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            Execution execution = (Execution) sync(cont -> new Functions(client())
                    .createExecution(functionId, json(payload), true, null, null, null,
                            scheduledAt, cont));
            return execution.getId();
        } catch (Exception e) {
            context.error("schedule failed: " + e);
            return null;
        }
    }

    /** Sends an email via Appwrite Messaging (provider configured in console). */
    public static String sendEmail(String to, String subject, String body,
                                   RuntimeContext context) {
        try {
            Users users = new Users(client());
            UserList<Map<String, Object>> found = userList(sync(cont ->
                    users.list(List.of(Query.Companion.limit(5)), to, null, mapClass(), cont)));
            User<Map<String, Object>> user = null;
            for (User<Map<String, Object>> candidate : found.getUsers()) {
                if (to.equalsIgnoreCase(candidate.getEmail())) {
                    user = candidate;
                    break;
                }
            }
            if (user == null) {
                user = (User<Map<String, Object>>) sync(cont -> users.create(
                        io.appwrite.ID.Companion.unique(7), to, null,
                        io.appwrite.ID.Companion.custom("pw-" + io.appwrite.ID.Companion.unique(7)),
                        null, mapClass(), cont));
            }
            String targetId = null;
            io.appwrite.models.TargetList targets = (io.appwrite.models.TargetList) sync(cont ->
                    users.listTargets(user.getId(), null, null, cont));
            for (Target target : targets.getTargets()) {
                if ("email".equals(target.getProviderType())
                        && to.equalsIgnoreCase(target.getIdentifier())) {
                    targetId = target.getId();
                    break;
                }
            }
            if (targetId == null) {
                Target created = (Target) sync(cont -> users.createTarget(user.getId(),
                        io.appwrite.ID.Companion.unique(7), MessagingProviderType.EMAIL,
                        to, null, null, cont));
                targetId = created.getId();
            }
            sync(cont -> new Messaging(client()).createEmail(
                    io.appwrite.ID.Companion.unique(7), subject, body,
                    null, null, List.of(targetId), null, null, null, null, null, null, cont));
            return null;
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    /**
     * Runs one SDK suspend call from Java and blocks until its result
     * arrives through the SDK's CoroutineCallback, which acts as the
     * continuation every Kotlin suspend function requires.
     */
    private static Object sync(SdkCall call) throws Exception {
        AtomicReference<Object> out = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        CoroutineCallback<Object> continuation = new CoroutineCallback<>((result, error) -> {
            out.set(error != null ? error : result);
            done.countDown();
        });
        call.invoke(continuation);
        if (!done.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Appwrite call timed out after 30s");
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

    /** A single SDK suspend call; may throw the SDK's checked exception. */
    @FunctionalInterface
    private interface SdkCall {
        Object invoke(CoroutineCallback<Object> continuation) throws Exception;
    }

    @SuppressWarnings("unchecked")
    private static RowList<Map<String, Object>> rowList(Object value) {
        return (RowList<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static UserList<Map<String, Object>> userList(Object value) {
        return (UserList<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static Class<Map<String, Object>> mapClass() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    private static TablesDB tables() {
        return new TablesDB(client());
    }

    /**
     * Tenant-isolated client. The runtime injects this function's project
     * id and dynamic API key; setting POCKET_TENANT_PROJECT / POCKET_TENANT_
     * KEY as function variables points the same code at another tenant's
     * project (per-tenant isolation through Appwrite projects).
     */
    private static Client client() {
        Client c = new Client();
        c.setEndpoint(firstNonBlank(env("APPWRITE_FUNCTION_API_ENDPOINT"), "http://localhost/v1"));
        c.setProject(firstNonBlank(
                firstNonBlank(env("POCKET_TENANT_PROJECT"), env("APPWRITE_FUNCTION_PROJECT_ID")),
                "main"));
        c.setKey(firstNonBlank(
                firstNonBlank(env("POCKET_TENANT_KEY"),
                        contextHeader("x-appwrite-key")),
                env("APPWRITE_FUNCTION_API_KEY")));
        return c;
    }

    /** Captured from the current request by the runtime bootstrap. */
    private static final ThreadLocal<Map<String, String>> REQUEST_HEADERS = new ThreadLocal<>();

    /** Installs the request headers so client() can read the dynamic key. */
    public static void bindHeaders(Map<String, String> headers) {
        REQUEST_HEADERS.set(headers);
    }

    private static String contextHeader(String name) {
        Map<String, String> headers = REQUEST_HEADERS.get();
        return headers != null ? headers.get(name) : null;
    }

    /** Substitutes {slot} placeholders in the stored template body. */
    private static String render(Row<Map<String, Object>> template, Map<String, Object> slots) {
        if (template == null) {
            return ""; // nothing configured; caller may skip replying
        }
        Object body = template.getData().get("body");
        String out = body != null ? String.valueOf(body) : "";
        for (Map.Entry<String, Object> e : slots.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return out;
    }

    /** Minimal JSON writer for execution payloads (keys are code-defined). */
    private static String json(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey().replace("\"", "\\\"")).append("\":");
            Object v = e.getValue();
            if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append('"').append(String.valueOf(v).replace("\"", "\\\"")).append('"');
            }
        }
        return sb.append('}').toString();
    }

    private static String env(String key) {
        String v = System.getenv(key);
        return v != null ? v : "";
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }
}
