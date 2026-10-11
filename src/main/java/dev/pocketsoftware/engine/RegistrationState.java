package dev.pocketsoftware.engine;

/** School registration flow state: step-by-step slot filling for one chat. */
public final class RegistrationState implements FlowState {

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
