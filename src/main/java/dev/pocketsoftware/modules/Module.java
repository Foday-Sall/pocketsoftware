package dev.pocketsoftware.modules;

import dev.pocketsoftware.engine.Appwrite;
import dev.pocketsoftware.engine.FlowState;
import java.util.List;

/** A self-contained conversation system mounted into the engine (school, hospital, business). */
public interface Module {

    /** Stable id used in config ('active_modules') and for logging. */
    String id();

    /** One-line name shown in the system menu. */
    String title();

    /** Menu entries; entry 1 must be the module's main flow, the rest answer immediately. */
    List<String> menuEntries();

    /** Wires the module to the shared database client, channel and config; called once at startup. */
    void init(Appwrite db, String channel, dev.pocketsoftware.Config config);

    /** Fresh per-chat state for this module's flow. */
    FlowState beginFlow();

    /** The flow's opening question (also the reply for menu entry 1). */
    String startMessage();

    /** Reply for a picked menu entry (1-based). */
    String onMenu(String chatId, int entry);

    /** True when this module wants to start its flow for this free-text command. */
    boolean handles(String text);

    /**
     * Handles one message: an active flow step when state belongs to this module, or a
     * classified command otherwise. userId is the chat's school id, null when unknown.
     */
    String handle(String chatId, String text, FlowState state, String userId);
}
