package dev.pocketsoftware.engine;

/** Result of classifying one message. */
public record Decision(String action, double confidence, String slot) {
}
