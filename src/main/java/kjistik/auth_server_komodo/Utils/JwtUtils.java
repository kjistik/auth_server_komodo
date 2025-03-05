package kjistik.auth_server_komodo.Utils;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kjistik.auth_server_komodo.Config.JwtConfig;

@Component
public class JwtUtils {

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

    public String generateJwtToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .claim("roles", roles)
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
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
