package dev.pocketsoftware.email;

import dev.pocketsoftware.engine.AppwriteSdk;
import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.enums.MessagingProviderType;
import io.appwrite.models.Target;
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
            return AppwriteSdk.run((scope, cont) -> {
                Client client = AppwriteSdk.client();
                Users users = new Users(client);
                Messaging messaging = new Messaging(client);

                // Find or create the Appwrite user for this address.
                User<?> user = null;
                for (User<?> candidate : users.list(null, to, null, cont).getUsers()) {
                    if (to.equalsIgnoreCase(candidate.getEmail())) {
                        user = candidate;
                        break;
                    }
                }
                if (user == null) {
                    user = users.create(ID.unique(), to, null, ID.unique() + "-pw", null, cont);
                }

                // Ensure the user has an email target, then send to it.
                String targetId = null;
                for (Target target : users.listTargets(user.getId(), null, cont).getTargets()) {
                    if ("email".equals(target.getProviderType())
                            && to.equalsIgnoreCase(target.getIdentifier())) {
                        targetId = target.getId();
                        break;
                    }
                }
                if (targetId == null) {
                    targetId = users.createTarget(user.getId(), ID.unique(),
                            MessagingProviderType.EMAIL, to, null, null, cont).getId();
                }

                messaging.createEmail(ID.unique(), subject, body,
                        null, null, List.of(targetId), null, null, null, null, null, null, cont);
                return null;
            });
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
