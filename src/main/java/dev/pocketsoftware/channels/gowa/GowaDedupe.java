package dev.pocketsoftware.channels.gowa;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers recently seen WhatsApp message ids so retries never
 * double-process one message (GOWA re-sends when the reply is slow).
 */
public final class GowaDedupe {

    private final Map<String, Boolean> seen = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > 1000; // cap memory on a long-running service
        }
    };

    /** True on first sight; false when the id was already processed. */
    public synchronized boolean firstTime(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return true; // nothing to dedupe on; treat as new
        }
        return seen.putIfAbsent(messageId, Boolean.TRUE) == null;
    }
}
