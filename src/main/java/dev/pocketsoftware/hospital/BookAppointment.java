package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.FlowState;

/** Appointment booking flow state for one chat. */
public final class BookAppointment implements FlowState {

    public String patientName;
    public String date;
    public String time;
    public String doctor;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
