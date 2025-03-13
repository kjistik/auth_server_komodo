package kjistik.auth_server_komodo.Services.RefreshToken;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class RefreshTokenService {
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    // Store a refresh token in Redis with a TTL (e.g., 30 days)
    public Mono<String> storeRefreshToken(String username, String refreshToken, String session) {
        Duration duration = Duration.of(30l, ChronoUnit.DAYS);
        String key = "refresh_token:" + username + ":" + session;
        return reactiveRedisTemplate.opsForValue().set(key, refreshToken, duration)
                .thenReturn(session);
    }

    // Retrieve the refresh token for a specific user and session
    public Mono<String> getRefreshToken(String username, String sessionId) {
        String key = "refresh_token:" + username + ":" + sessionId;
        return reactiveRedisTemplate.opsForValue().get(key)
                .switchIfEmpty(Mono.just("")); // Return an empty string if the token doesn't exist
    }

    // Delete a refresh token (e.g., during logout)
    public Mono<Boolean> deleteRefreshToken(String username, String sessionId) {
        String key = "refresh_token:" + username + ":" + sessionId;
        return reactiveRedisTemplate.opsForValue().delete(key);
    }
}