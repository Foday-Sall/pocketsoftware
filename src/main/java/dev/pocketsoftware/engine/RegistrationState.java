package dev.pocketsoftware.engine;

/** Registration flow: step-by-step slot filling for one chat. */
final class RegistrationState {
    final String channel;
    final String chatId;
    String fullName;
    String classId;
    String guardianPhone;
    String guardianName;

    RegistrationState(String channel, String chatId) {
        this.channel = channel;
        this.chatId = chatId;
    }
}
