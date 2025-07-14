package kjistik.auth_server_komodo.Utils;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey; // This will now be Mono<PublicKey> from the bean
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import kjistik.auth_server_komodo.Config.JwtConfig;
import kjistik.auth_server_komodo.Exceptions.JwtAuthenticationException;
import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import kjistik.auth_server_komodo.Services.Vault.VaultSigningService;
import lombok.Getter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtConfig jwtConfig;
    private final RefreshTokenService refreshTokenService;
    private final VaultSigningService vaultSigningService;
    private final Mono<PublicKey> publicKeyMono;
    private PublicKey cachedPublicKey;

    public JwtUtils(JwtConfig jwtConfig,
            RefreshTokenService refreshTokenService,
            VaultSigningService vaultSigningService,
            @Qualifier("vaultPublicKey") Mono<PublicKey> publicKeyMono) {
        this.jwtConfig = jwtConfig;
        this.refreshTokenService = refreshTokenService;
        this.vaultSigningService = vaultSigningService;
        this.publicKeyMono = publicKeyMono;
    }

    // @PostConstruct is acceptable here because it runs once at application
    // startup.
    // We will block on the publicKeyMono here.
    @PostConstruct
    public void validateAndCachePublicKey() {
        try {
            this.cachedPublicKey = publicKeyMono.block(); // Block once at startup to get the key
            if (this.cachedPublicKey == null) {
                throw new IllegalStateException(
                        "Failed to retrieve public key from Vault during JwtUtils initialization.");
            }
            if (!"EC".equals(this.cachedPublicKey.getAlgorithm())) {
                throw new IllegalStateException(
                        "Public key must be an EC key, but found: " + this.cachedPublicKey.getAlgorithm());
            }
            log.info("Public key validated and cached successfully: Algorithm=EC");
        } catch (Exception e) {
            log.error("Error during public key validation/caching: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize JwtUtils due to public key error.", e);
        }
    }

    public Jws<Claims> validateTokenToleratingExpired(String token) {
        if (this.cachedPublicKey == null) {
            log.error("Public key is not initialized in JwtUtils. Cannot validate token.");
            throw new JwtAuthenticationException("Server error: Public key not initialized.");
        }
        try {
            log.debug("Validating token structure and signature (tolerating expired)");
            return Jwts.parser()
                    .clockSkewSeconds(Integer.MAX_VALUE)
                    .verifyWith(this.cachedPublicKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SecurityException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid token signature", e);
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            throw new JwtAuthenticationException("Malformed token", e);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid token argument: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid token format", e);
        }
    }

    public Mono<JwtResponse> generateJwtToken(String username, String session, List<String> roles, String agent,
            String os, String resolution, String timezone) {
        return DeviceFingerprintUtils.generateFingerprint(agent, timezone, os, resolution)
                .flatMap(fingerprint -> generateRefreshToken(username)
                        .flatMap(refreshToken -> refreshTokenService.storeRefreshToken(
                                username,
                                refreshToken,
                                fingerprint,
                                session)
                                .flatMap(newSessionId -> {
                                    String unsignedToken = buildUnsignedToken(username, roles,
                                            jwtConfig.getExpirationTime());
                                    return signToken(unsignedToken)
                                            .map(signedToken -> new JwtResponse(signedToken, newSessionId));
                                })));
    }

    public Mono<String> generateRefreshToken(String username) {
        String unsignedToken = buildUnsignedToken(username, Collections.emptyList(),
                jwtConfig.getRefreshExpirationTime());
        return signToken(unsignedToken);
    }

    public Mono<String> generateVerificationToken(UUID id) {
        String unsignedToken = buildUnsignedToken(id.toString(), Collections.emptyList(),
                jwtConfig.getVerificationExpirationTime());
        return signToken(unsignedToken);
    }

    private String buildUnsignedToken(String subject, List<String> roles, long expirationTime) {
        long issuedAt = System.currentTimeMillis();
        long expiration = issuedAt + expirationTime;

        String headerJson = String.format("""
                {
                  "alg": "ES256",
                  "typ": "JWT",
                  "kid": "vault-key-1"
                }
                """);

        String rolesArray = roles.isEmpty() ? "[]" : "[\"" + String.join("\",\"", roles) + "\"]";

        String payloadJson = String.format("""
                {
                  "sub": "%s",
                  "iat": %d,
                  "exp": %d,
                  "roles": %s
                }
                """,
                escapeJson(subject),
                issuedAt / 1000,
                expiration / 1000,
                rolesArray);

        String base64Header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String base64Payload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        return base64Header + "." + base64Payload;
    }

    private String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Mono<String> signToken(String unsignedToken) {
        return vaultSigningService.sign(unsignedToken)
                .map(signature -> unsignedToken + "." + signature)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Token signing failed: {}", e.getMessage(), e));
    }


    public UUID extractUserIdFromToken(String token) {
        if (this.cachedPublicKey == null) {
            log.error("Public key is not initialized in JwtUtils. Cannot extract user ID.");
            throw new RuntimeException("Server error: Public key not initialized.");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(this.cachedPublicKey) 
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage(), e);
        }
    }

    public List<String> extractRolesFromExpiredToken(String token) {
        if (this.cachedPublicKey == null) {
            log.error("Public key is not initialized in JwtUtils. Cannot extract roles from expired token.");
            throw new JwtException("Server error: Public key not initialized.");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(this.cachedPublicKey)
                    .clockSkewSeconds(Integer.MAX_VALUE)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return parseRolesClaim(claims.get("roles"));
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Failed to extract roles from token", e);
        }
    }

    private List<String> parseRolesClaim(Object rolesClaim) {
        if (rolesClaim instanceof List) {
            return ((List<?>) rolesClaim).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        } else if (rolesClaim instanceof String) {
            return List.of(((String) rolesClaim).split(","));
        }
        return Collections.emptyList();
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    @Getter
    public static class JwtResponse {
        private final String token;
        private final String sessionId;

        public JwtResponse(String token, String sessionId) {
            this.token = token;
            this.sessionId = sessionId;
        }
    }
}