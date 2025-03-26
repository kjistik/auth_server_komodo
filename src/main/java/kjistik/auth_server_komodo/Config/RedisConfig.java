package kjistik.auth_server_komodo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import kjistik.auth_server_komodo.Utils.DatabaseEntities.RefreshTokenValue;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, RefreshTokenValue> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        
        // Configure JSON serializer for values
        Jackson2JsonRedisSerializer<RefreshTokenValue> valueSerializer = 
            new Jackson2JsonRedisSerializer<>(RefreshTokenValue.class);
        
        RedisSerializationContext<String, RefreshTokenValue> serializationContext = 
            RedisSerializationContext.<String, RefreshTokenValue>newSerializationContext()
                .key(new StringRedisSerializer())
                .value(valueSerializer)
                .hashKey(new StringRedisSerializer())
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}