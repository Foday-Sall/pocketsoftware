package dev.pocketsoftware.engine;

/** Per-chat conversation state (registration flow or plain identity). */
final class ConversationState {
    RegistrationState registration;
    String studentId;
}
