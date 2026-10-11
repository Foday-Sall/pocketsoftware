package dev.pocketsoftware.engine;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Runs a task once per day at a fixed local time. */
public final class DailyJob {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "daily-job");
        t.setDaemon(true);
        return t;
    });

    /** Starts the job; it fires at the given time (zone/system default) every day. */
    public void startAt(String hhmm, Runnable task) {
        String[] parts = hhmm.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        LocalTime target = LocalTime.of(hour, minute);
        long initialDelay = secondsUntilNext(target);
        executor.scheduleWithFixedDelay(task, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private static long secondsUntilNext(LocalTime target) {
        var now = LocalTime.now();
        var today = java.time.Duration.between(now, target).getSeconds();
        return today > 0 ? today : today + TimeUnit.DAYS.toSeconds(1);
    }
}
