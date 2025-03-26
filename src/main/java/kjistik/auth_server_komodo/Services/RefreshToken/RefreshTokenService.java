package kjistik.auth_server_komodo.Services.RefreshToken;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import kjistik.auth_server_komodo.Utils.DatabaseEntities.RefreshTokenValue;
import reactor.core.publisher.Mono;

@Service
public class RefreshTokenService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public RefreshTokenService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<String> storeRefreshToken(String username, 
                                        String refreshToken, 
                                        String fingerprint, 
                                        String session) {
        Duration duration = Duration.of(30, ChronoUnit.DAYS);
        String key = "refresh_token:" + username + ":" + session;
        
        // Create value object
        RefreshTokenValue value = new RefreshTokenValue(refreshToken, fingerprint);
        
        return reactiveRedisTemplate.opsForValue()
                .set(key, value, duration)
                .thenReturn(session);
    }

    public Mono<RefreshTokenValue> getRefreshToken(String username, String sessionId) {
        String key = "refresh_token:" + username + ":" + sessionId;
        return reactiveRedisTemplate.opsForValue().get(key)
                .cast(RefreshTokenValue.class);
    }
}