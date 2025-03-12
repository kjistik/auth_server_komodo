package kjistik.auth_server_komodo.Services.RefreshToken;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class RefreshTokenService {
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    // Store a refresh token in Redis with a TTL (e.g., 30 days)
    public Mono<Boolean> storeRefreshToken(String username, String refreshToken) {
        Duration duration = Duration.of(30l, ChronoUnit.DAYS);
        String key = "refresh_token:" + username;
        return reactiveRedisTemplate.opsForValue().set(key, refreshToken, duration);
    }

    // Check if a valid refresh token exists for the user
    public Mono<Boolean> hasValidRefreshToken(String username) {
        String key = "refresh_token:" + username;
        return reactiveRedisTemplate.opsForValue().get(key).hasElement();
    }

    // Delete a refresh token (e.g., during logout)
    public Mono<Boolean> deleteRefreshToken(String username) {
        String key = "refresh_token:" + username;
        return reactiveRedisTemplate.opsForValue().delete(key);
    }
}