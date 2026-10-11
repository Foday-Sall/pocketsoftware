package dev.pocketsoftware.engine;

import io.appwrite.Client;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;

/**
 * Single entry point to the official Appwrite Kotlin SDK from Java.
 *
 * Holds one shared Client configured for our self-hosted server (endpoint +
 * project ID + server API key from config) and exposes run(), which executes
 * SDK suspend functions from Java via runBlocking.
 */
public final class AppwriteSdk {

    private static volatile Client client;

    private AppwriteSdk() {
    }

    /**
     * Configures the shared SDK client. Call once at startup with values from
     * config: appwrite.endpoint (self-hosted, e.g. https://api.example.dev/v1),
     * appwrite.project and appwrite.key (server API key, never client-side).
     */
    public static Client init(String endpoint, String projectId, String apiKey) {
        Client c = new Client(endpoint, false);
        c.setProject(projectId);
        c.setKey(apiKey);
        client = c;
        return c;
    }

    /** Shared client; throws if init() has not run. */
    public static Client client() {
        Client c = client;
        if (c == null) {
            throw new IllegalStateException("AppwriteSdk.init() not called");
        }
        return c;
    }

    /**
     * Runs one SDK suspend call from Java, blocking until it completes.
     *
     * @param block receives the CoroutineScope the SDK suspend functions need;
     *              e.g. {@code Appwrite.run(scope -> new TablesDB(scope).listRows(...))}
     */
    public static <T> T run(Function2<? super CoroutineScope, ? super CoroutineContext, ? extends T> block) {
        // kotlinx.coroutines runBlocking(Function2, CoroutineContext) — the Java signature.
        return kotlinx.coroutines.BuildersKt.runBlocking(block, null);
    }
}
