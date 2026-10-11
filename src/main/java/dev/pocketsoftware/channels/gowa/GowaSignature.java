package dev.pocketsoftware.channels.gowa;

import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies GOWA's webhook signature.
 *
 * GOWA signs every webhook body with HMAC-SHA256 and sends it in the
 * X-Hub-Signature-256 header as "sha256={hex}". The comparison is
 * constant-time so response timing cannot leak the expected value.
 */
public final class GowaSignature {

    private GowaSignature() {
    }

    /** True when the header proves the body was signed with our secret. */
    public static boolean isValid(byte[] body, String header, String secret) {
        if (header == null || secret == null || secret.isBlank()) {
            return false;
        }
        String expected = hmacSha256(body, secret);
        return MessageDigest.isEqual(
                ("sha256=" + expected).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String hmacSha256(byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(body);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }
}
