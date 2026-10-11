package dev.pocketsoftware.setup;

/** User-Agent strings used by this project's HTTP calls. */
final class UserAgents {

    /** Cloudflare on api.pocketsoftware.dev rejects the default Java UA. */
    static final String BROWSER =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private UserAgents() {
    }
}
