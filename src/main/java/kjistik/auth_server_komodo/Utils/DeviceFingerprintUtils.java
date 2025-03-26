package kjistik.auth_server_komodo.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

import reactor.core.publisher.Mono;

public class DeviceFingerprintUtils {

    public static Mono<String> generateFingerprint(String agent, String timezone, String os, String resolution) {
        return Mono.fromCallable(() -> {

            validateHeaders(agent, timezone, os, resolution);

            String trimmedUA = trimUserAgent(agent);
            String fingerprintData = String.join("|", trimmedUA, timezone, os, resolution);
            return DigestUtils.sha256Hex(fingerprintData);
        });
    }

    private static String trimUserAgent(String userAgent) {
        Pattern pattern = Pattern.compile("([^/\\s]+/\\d+)\\.?[^\\s]*");
        Matcher matcher = pattern.matcher(userAgent);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    private static void validateHeaders(String... headers) {
        for (String header : headers) {
            if (header == null || header.isBlank()) {
                throw new IllegalArgumentException("Missing device fingerprint headers");
            }
        }
    }
}