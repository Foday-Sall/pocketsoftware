package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.FlowState;

/** Patient registration flow state for one chat. */
public final class PatientFlow implements FlowState {

    public String fullName;
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
