package dev.pocketsoftware.business;

import dev.pocketsoftware.engine.FlowState;

/** Sale-recording flow state for one chat. */
public final class RecordSale implements FlowState {

    public String customerName;
    public String item;
    public String amountText;
    public String paymentMethod;

    private boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }
}
