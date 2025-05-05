package kjistik.auth_server_komodo.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import kjistik.auth_server_komodo.Config.JwtConfig;
import kjistik.auth_server_komodo.Exceptions.JwtAuthenticationException;
import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Autowired
    RefreshTokenService service;

    private final JwtConfig jwtConfig;

    private final SecretKey secretKey;

    public JwtUtils(JwtConfig jwtConfig, SecretKey secretKey) {
        this.jwtConfig = jwtConfig;
        this.secretKey = secretKey;
    }

    public Jws<Claims> validateTokenToleratingExpired(String token) {
        try {
            log.debug("Validating token structure and signature");
            return Jwts.parser()
                    .clockSkewSeconds(Integer.MAX_VALUE)// Disables expiration checks
                    .verifyWith(secretKey)
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

    // Return both JWT and session ID
    public Mono<JwtResponse> generateJwtToken(String username, String session, List<String> roles, String agent,
            String os, String resolution, String timezone) {
        return DeviceFingerprintUtils.generateFingerprint(agent, timezone, os, resolution)
                .flatMap(fingerprint -> service.storeRefreshToken(
                        username,
                        generateRefreshToken(username),
                        fingerprint, // Now using the resolved String value
                        session)
                        .flatMap(newSessionId -> {
                            // Generate JWT with session ID in claims
                            String token = Jwts.builder()
                                    .subject(username)
                                    .issuedAt(new Date())
                                    .claim("roles", roles)
                                    .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
                                    .signWith(secretKey)
                                    .compact();
                            return Mono.just(new JwtResponse(token, newSessionId));
                        }));
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpirationTime()))
                .signWith(secretKey)
                .compact();
    }

    public String generateVerificationToken(UUID id) {
        return Jwts.builder()
                .subject(id.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getVerificationExpirationTime()))
                .signWith(secretKey)
                .compact();
    }

    /*
     * public List<String> extractRolesFromToken(String token) {
     * try {
     * Claims claims = Jwts.parser()
     * .verifyWith(secretKey)
     * .build()
     * .parseSignedClaims(token)
     * .getPayload();
     * 
     * // Extract roles claim and handle different possible formats
     * Object rolesClaim = claims.get("roles");
     * 
     * if (rolesClaim instanceof List) {
     * // Handle case where roles are stored as List<String>
     * return ((List<?>) rolesClaim).stream()
     * .filter(String.class::isInstance)
     * .map(String.class::cast)
     * .toList();
     * } else if (rolesClaim instanceof String) {
     * // Handle case where roles are stored as comma-separated string
     * return Arrays.asList(((String) rolesClaim).split(","));
     * }
     * 
     * return Collections.emptyList();
     * } catch (JwtException | IllegalArgumentException e) {
     * throw new JwtException("Failed to extract roles from token", e);
     * }
     * }
     */
    public UUID extractUserIdFromToken(String token) {
        try {
            // Parse the token and extract the claims
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey) // Verify the token's signature
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract the subject (user ID) from the claims
            String userIdString = claims.getSubject();

            // Convert the subject to a UUID
            return UUID.fromString(userIdString);
        } catch (Exception e) {
            // Handle invalid tokens (e.g., expired, tampered, etc.)
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    public List<String> extractRolesFromExpiredToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .clockSkewSeconds(Integer.MAX_VALUE)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract roles claim and handle different possible formats
            Object rolesClaim = claims.get("roles");

            if (rolesClaim instanceof List) {
                // Handle case where roles are stored as List<String>
                return ((List<?>) rolesClaim).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            } else if (rolesClaim instanceof String) {
                // Handle case where roles are stored as comma-separated string
                return Arrays.asList(((String) rolesClaim).split(","));
            }

            return Collections.emptyList();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Failed to extract roles from token", e);
        }
    }

    // Helper class to hold both JWT and session ID
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