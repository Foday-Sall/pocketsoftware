package dev.pocketsoftware.setup;

/** Raised when Appwrite answers with an HTTP status >= 300. */
class RestException extends RuntimeException {

    final int status;
    final String body;

    RestException(String method, String path, int status, String body) {
        super(method + " " + path + " -> " + status + " " + body);
        this.status = status;
        this.body = body;
    }
}
