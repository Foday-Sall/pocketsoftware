package dev.pocketsoftware.setup;

/** Any provisioning failure (REST error, timeout, bad config). Message is user-facing. */
class ProvisioningException extends RuntimeException {

    ProvisioningException(String message) {
        super(message);
    }
}
