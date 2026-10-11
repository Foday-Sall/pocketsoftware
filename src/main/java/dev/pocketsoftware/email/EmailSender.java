package dev.pocketsoftware.email;

import jakarta.mail.Message.RecipientType;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.RecipientBuilder;
import org.simplejavamail.mailer.MailerBuilder;

/** Sends email over SMTP using simple-java-mail; all settings come from config. */
public final class EmailSender {

    private final String host;
    private final int port;
    private final String user;
    private final String pass;
    private final String from;

    public EmailSender(String host, int port, String user, String pass, String from) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.from = from;
    }

    /** True when SMTP is configured; features that need email check this first. */
    public boolean isConfigured() {
        return host != null && !host.isBlank() && pass != null && !pass.isBlank();
    }

    /** Sends one plain-text email; returns error text or null on success. */
    public String send(String to, String subject, String body) {
        if (!isConfigured()) {
            return "email not configured";
        }
        try (Mailer mailer = MailerBuilder
                .withSMTPServer(host, port, user, pass)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10_000)
                .buildMailer()) {
            mailer.sendMail(EmailBuilder.startingBlank()
                    .from(from)
                    .withRecipients(new RecipientBuilder()
                            .withAddress(to)
                            .withType(RecipientType.TO)
                            .build())
                    .withSubject(subject)
                    .withPlainText(body)
                    .buildEmail());
            return null;
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
