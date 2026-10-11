package dev.pocketsoftware.engine;

/** Per-chat flow state; the router clears the flow once isDone() is true. */
public interface FlowState {
    boolean isDone();
}
