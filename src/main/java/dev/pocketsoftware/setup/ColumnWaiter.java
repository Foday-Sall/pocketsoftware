package dev.pocketsoftware.setup;

import com.eclipsesource.json.JsonObject;

/** Polls a freshly created column until Appwrite reports it available. */
final class ColumnWaiter {

    private static final long TIMEOUT_MS = 60_000;
    private static final long POLL_MS = 300;

    /** Waits until the column is available, or fails after 60 seconds. */
    static void await(RestClient rest, String tablePath, String label, String key) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            JsonObject column = rest.get(tablePath + "/columns/" + key);
            if ("available".equals(RestJson.str(column, "status"))) {
                return;
            }
            sleep(label);
        }
        throw new ProvisioningException("Column " + label + " did not become available within "
                + (TIMEOUT_MS / 1000) + "s.");
    }

    private static void sleep(String label) {
        try {
            Thread.sleep(POLL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProvisioningException("Interrupted while waiting for column " + label);
        }
    }

    private ColumnWaiter() {
    }
}
