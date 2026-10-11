package dev.pocketsoftware.engine;

import dev.pocketsoftware.Config;
import dev.pocketsoftware.modules.Modules;
import dev.pocketsoftware.modules.Module;
import java.util.HashMap;
import java.util.Map;

/** Routes messages through the active modules; one flow per chat at a time. */
public final class Router {

    private final Map<String, ConversationState> chats = new HashMap<>();
    private final Modules modules;
    private final AuditLog logs;
    private final String channel;

    public Router(Modules modules, Appwrite db, String channel, Config config) {
        this.modules = modules;
        this.logs = new AuditLog(db);
        this.channel = channel;
        for (Module m : modules.all()) {
            m.init(db, channel, config);
        }
    }

    /** Handles one incoming message; returns the reply text. */
    public String handle(String chatId, String text) {
        logs.message(chatId, "in", channel, text, "raw", 0.0);
        ConversationState state = chats.computeIfAbsent(chatId, id -> new ConversationState());
        if (state.flowState == null && state.studentId == null) {
            state.studentId = Identity.studentId(modules, channel, chatId);
        }
        String reply = replyFor(state, chatId, text);
        logs.message(chatId, "out", channel, reply, "reply", 1.0);
        return reply;
    }

    /** One chat turn: active flow, greeting, menu digit, module keyword, or unknown. */
    private String replyFor(ConversationState state, String chatId, String text) {
        if (state.flowState != null) {
            return continueFlow(state, chatId, text);
        }
        if (TextCommands.isGreeting(text)) {
            return Menus.welcome(modules);
        }
        int digit = TextCommands.asDigit(text);
        if (digit > 0) {
            return forDigit(state, chatId, digit);
        }
        for (Module m : modules.all()) {
            if (m.handles(text)) {
                return startFlow(state, m);
            }
        }
        return unknownReply();
    }

    /** Menu digits are global 1..N across the active modules, in config order. */
    private String forDigit(ConversationState state, String chatId, int digit) {
        int remaining = digit;
        for (Module m : modules.all()) {
            int count = m.menuEntries().size();
            if (remaining <= count) {
                return remaining == 1 ? startFlow(state, m) : m.onMenu(chatId, remaining);
            }
            remaining -= count;
        }
        return unknownReply();
    }

    private String continueFlow(ConversationState state, String chatId, String text) {
        Module m = modules.byId(state.flowModule);
        if (m == null) {
            state.clearFlow();
            return unknownReply();
        }
        String reply = m.handle(chatId, text, state.flowState, state.studentId);
        if (state.flowState != null && state.flowState.isDone()) {
            state.clearFlow();
        }
        return reply;
    }

    private String startFlow(ConversationState state, Module m) {
        state.flowModule = m.id();
        state.flowState = m.beginFlow();
        return m.startMessage();
    }

    private String unknownReply() {
        return "I didn't catch that.\n" + Menus.welcome(modules);
    }
}