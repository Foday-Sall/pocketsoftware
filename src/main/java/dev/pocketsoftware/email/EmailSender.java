package dev.pocketsoftware.email;

import dev.pocketsoftware.engine.AppwriteSdk;
import io.appwrite.ID;
import io.appwrite.models.User;
import io.appwrite.services.Messaging;
import io.appwrite.services.Users;
import java.util.List;

/**
 * Sends email through Appwrite Messaging, using the official Kotlin SDK.
 * The provider (Mailgun/Sendgrid/SMTP) is configured once in the Appwrite
 * console; no mail settings live in this codebase.
 */
public final class EmailSender {

    /** Sends one plain-text email; returns error text or null on success. */
    public String send(String to, String subject, String body) {
        if (to == null || !to.contains("@")) {
            return "invalid email address";
        }
        try {
            return AppwriteSdk.run((scope, continuation) -> {
                Users users = new Users(scope);
                Messaging messaging = new Messaging(scope);

                // Find or create the Appwrite user for this address.
                User<?> user = null;
                for (User<?> candidate : users.list(null, to, null).getUsers()) {
                    if (to.equalsIgnoreCase(candidate.getEmail())) {
                        user = candidate;
                        break;
                    }
                }
                if (user == null) {
                    user = users.create(ID.unique(), to, null, ID.unique() + "-pw", null);
                }

                // Ensure the user has an email target, then send to it.
                String targetId = null;
                for (var target : users.listTargets(user.getId(), null).getTargets()) {
                    if ("email".equals(target.getProviderType())
                            && to.equalsIgnoreCase(target.getIdentifier())) {
                        targetId = target.getId();
                        break;
                    }
                }
                if (targetId == null) {
                    targetId = users.createTarget(
                            user.getId(), ID.unique(),
                            io.appwrite.enums.MessagingProviderType.EMAIL, to
                    ).getId();
                }

                messaging.createEmail(ID.unique(), subject, body, null, null, List.of(targetId));
                return null;
            });
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
