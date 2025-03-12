package kjistik.auth_server_komodo.Utils;

import java.util.Base64;
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

    public Mono<String> generateJwtToken(String username, List<String> roles) {
        // Delete old refresh token, store new refresh token, and generate a new JWT
        // token
        return service.deleteRefreshToken(username)
                .doOnTerminate(() -> System.out.println("Deleting old refresh token for user: " + username))
                .then(service.storeRefreshToken(username, generateRefreshToken(username))
                        .doOnTerminate(() -> System.out.println("Storing new refresh token for user: " + username))
                        .then(Mono.fromCallable(() -> {
                            System.out.println("Generating JWT for user: " + username);
                            return Jwts.builder()
                                    .subject(username)
                                    .issuedAt(new Date())
                                    .claim("roles", roles)
                                    .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
                                    .signWith(getSecretKey())
                                    .compact();
                        })));
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
}
