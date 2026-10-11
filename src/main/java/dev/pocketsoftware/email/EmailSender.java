package dev.pocketsoftware.email;

import dev.pocketsoftware.engine.AppwriteSdk;
import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.enums.MessagingProviderType;
import io.appwrite.models.Target;
import io.appwrite.models.User;
import io.appwrite.models.UserList;
import io.appwrite.services.Messaging;
import io.appwrite.services.Users;
import java.util.List;
import java.util.Map;

/**
 * Sends email through Appwrite Messaging, using the official Kotlin SDK.
 * The provider (Mailgun/Sendgrid/SMTP) is configured once in the Appwrite
 * console; no mail settings live in this codebase.
 *
 * Two JVM-level quirks of calling Kotlin suspend functions from Java are
 * handled here: results are Object (cast explicitly), and the SDK declares
 * throws AppwriteException on every call (caught inside the lambda, because
 * the Function2 target type cannot declare it).
 */
public final class EmailSender {

    /** Sends one plain-text email; returns error text or null on success. */
    public String send(String to, String subject, String body) {
        if (to == null || !to.contains("@")) {
            return "invalid email address";
        }
        try {
            return AppwriteSdk.run((scope, cont) -> {
                try {
                    Client client = AppwriteSdk.client();
                    Users users = new Users(client);
                    Messaging messaging = new Messaging(client);

                    // Find or create the Appwrite user for this address.
                    UserList<Map<String, Object>> found = (UserList<Map<String, Object>>) (Object)
                            users.list((List<String>) null, to, (Boolean) null, cont);
                    User<Map<String, Object>> user = null;
                    for (User<Map<String, Object>> candidate : found.getUsers()) {
                        if (to.equalsIgnoreCase(candidate.getEmail())) {
                            user = candidate;
                            break;
                        }
                    }
                    if (user == null) {
                        user = (User<Map<String, Object>>) (Object) users.create(
                                ID.Companion.unique(7), to, null,
                                ID.Companion.custom("pw-" + ID.Companion.unique(7)), null,
                                (Class<Map<String, Object>>) (Class<?>) Map.class, cont);
                    }

                    // Ensure the user has an email target, then send to it.
                    io.appwrite.models.TargetList targets = (io.appwrite.models.TargetList) (Object)
                            users.listTargets(user.getId(), (List<String>) null, (Boolean) null, cont);
                    String targetId = null;
                    for (Target target : targets.getTargets()) {
                        if ("email".equals(target.getProviderType())
                                && to.equalsIgnoreCase(target.getIdentifier())) {
                            targetId = target.getId();
                            break;
                        }
                    }
                    if (targetId == null) {
                        targetId = ((Target) (Object) users.createTarget(user.getId(),
                                ID.Companion.unique(7), MessagingProviderType.EMAIL,
                                to, null, null, cont)).getId();
                    }

                    messaging.createEmail(ID.Companion.unique(7), subject, body,
                            (List<String>) null, (List<String>) null, List.of(targetId),
                            (List<String>) null, (List<String>) null, (List<String>) null,
                            (Boolean) null, (Boolean) null, (String) null, cont);
                    return null;
                } catch (Exception e) {
                    // SDK calls declare throws AppwriteException; the lambda's
                    // target type cannot, so catch here and report as text.
                    return e.getClass().getSimpleName() + ": " + e.getMessage();
                }
            });
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
