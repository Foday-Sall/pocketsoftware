package dev.pocketsoftware.hospital;

import dev.pocketsoftware.engine.DailyJob;
import dev.pocketsoftware.engine.MessageSender;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Daily cron: reminds patients of today's appointments. */
public final class Reminders {

    private final AppointmentStore appointments;
    private final PatientStore patients;
    private final MessageSender sender;
    private final Map<String, String> chatByPhone = new ConcurrentHashMap<>();

    public Reminders(AppointmentStore appointments, PatientStore patients, MessageSender sender) {
        this.appointments = appointments;
        this.patients = patients;
        this.sender = sender;
    }

    /** Tells the reminder which chat belongs to which patient phone. */
    public void registerChat(String phone, String chatId) {
        chatByPhone.put(phone, chatId);
    }

    /** Starts the daily check at the given time (HH:MM). */
    public void startDaily(String hhmm) {
        new DailyJob().startAt(hhmm, this::sendTodaysReminders);
    }

    /** Sends one reminder per patient with an appointment today. */
    void sendTodaysReminders() {
        try {
            String today = LocalDate.now().toString();
            for (var a : appointments.forDate(today)) {
                var p = patients.byId(a.patientId());
                if (p == null || p.phone() == null || p.phone().isBlank()) {
                    continue;
                }
                String chatId = chatByPhone.get(p.phone());
                if (chatId == null) {
                    continue;
                }
                String time = a.time() == null || a.time().isBlank() ? "" : " at " + a.time();
                sender.send(chatId, "⏰ Reminder: you have an appointment today ("
                        + today + ")" + time
                        + (a.doctor().isBlank() ? "" : " with " + a.doctor()) + ".");
            }
        } catch (Exception e) {
            System.err.println("Appointment reminders failed: " + e.getMessage());
        }
    }
}
