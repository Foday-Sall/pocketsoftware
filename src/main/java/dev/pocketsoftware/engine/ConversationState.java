package dev.pocketsoftware.engine;

/** Per-chat state: an active module flow or just the resolved school identity. */
public final class ConversationState {

    public String flowModule;
    public FlowState flowState;
    String studentId;

    void clearFlow() {
        flowModule = null;
        flowState = null;
    }
}
