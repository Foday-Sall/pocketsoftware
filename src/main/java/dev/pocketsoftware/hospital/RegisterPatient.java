package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.FlowState;

/** Patient registration flow state for one chat. */
public final class RegisterPatient implements FlowState {

    public String fullName;
    public String phone;
    public String gender;
    public String ageText;
    public String address;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
