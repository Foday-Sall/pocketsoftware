package dev.pocketsoftware.school;

import dev.pocketsoftware.engine.FlowState;

/** Student registration flow state: step-by-step slot filling for one chat. */
public final class RegisterForm implements FlowState {

    public String fullName;
    public String classId;
    public String guardianPhone;
    public String guardianName;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
