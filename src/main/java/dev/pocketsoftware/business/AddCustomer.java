package dev.pocketsoftware.business;

import dev.pocketsoftware.engine.FlowState;

/** Customer-add flow state for one chat. */
public final class AddCustomer implements FlowState {

    public String name;
    public String phone;
    public String debtText;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
