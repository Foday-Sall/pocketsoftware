package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.FlowState;

/** Visit-opening flow state for one chat. */
public final class BookVisit implements FlowState {

    public String patientName;
    public String complaint;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
