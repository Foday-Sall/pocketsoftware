package dev.pocketsoftware.engine;

/** Classifies incoming text into an action. */
public interface Classifier {
    Decision classify(String text);
}
