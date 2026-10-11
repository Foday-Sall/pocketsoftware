package dev.pocketsoftware.business;

import dev.pocketsoftware.engine.FlowState;

/** Customer registration flow state for one chat. */
public final class CustomerFlow implements FlowState {

    public String name;
    public String phone;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
