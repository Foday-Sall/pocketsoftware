package dev.pocketsoftware.email;

import dev.pocketsoftware.engine.AppwriteSdk;
import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.enums.MessagingProviderType;
import io.appwrite.models.Target;
import io.appwrite.models.User;
import io.appwrite.services.Messaging;
import io.appwrite.services.Users;
import java.util.Map;
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
                for (User<?> candidate : users.list((List<String>) null, to, (Boolean) null, (kotlin.coroutines.Continuation) cont).getUsers()) {
                    if (to.equalsIgnoreCase(candidate.getEmail())) {
                        user = candidate;
                        break;
                    }
                }
                if (user == null) {
                    user = users.create(ID.Companion.unique(7), to, null,
                            ID.Companion.custom("pw-" + ID.Companion.unique(7)), null,
                            (Class<Map<String, Object>>) (Class<?>) Map.class, cont);
                }

                // Ensure the user has an email target, then send to it.
                String targetId = null;
                for (Target target : users.listTargets(user.getId(), (List<String>) null, (Boolean) null, (kotlin.coroutines.Continuation) cont).getTargets()) {
                    if ("email".equals(target.getProviderType())
                            && to.equalsIgnoreCase(target.getIdentifier())) {
                        targetId = target.getId();
                        break;
                    }
                }
                if (targetId == null) {
                    targetId = users.createTarget(user.getId(), ID.Companion.unique(7),
                            MessagingProviderType.EMAIL, to, null, null, (kotlin.coroutines.Continuation) cont).getId();
                }

                messaging.createEmail(ID.Companion.unique(7), subject, body,
                        (List<String>) null, (List<String>) null, List.of(targetId),
                        (List<String>) null, (List<String>) null, (List<String>) null,
                        (Boolean) null, (Boolean) null, (String) null, (kotlin.coroutines.Continuation) cont);
                return null;
            });
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
