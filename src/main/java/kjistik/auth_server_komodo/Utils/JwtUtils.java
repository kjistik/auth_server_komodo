package kjistik.auth_server_komodo.Utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kjistik.auth_server_komodo.Config.JwtConfig;
import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Component
public class JwtUtils {

    @Autowired
    RefreshTokenService service;

    private final JwtConfig jwtConfig;

    public JwtUtils(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Jws<Claims> validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey()) // Validate signature
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("token", e); // Invalid token
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
                                    .signWith(getSecretKey())
                                    .compact();
                            return Mono.just(new JwtResponse(token, newSessionId));
                        }));
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpirationTime()))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateVerificationToken(UUID id) {
        return Jwts.builder()
                .subject(id.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getVerificationExpirationTime()))
                .signWith(getSecretKey())
                .compact();
    }

    public List<String> extractRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
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

    public UUID extractUserIdFromToken(String token) {
        try {
            // Parse the token and extract the claims
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey()) // Verify the token's signature
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
