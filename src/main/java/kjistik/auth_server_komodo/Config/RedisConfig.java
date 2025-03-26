package kjistik.auth_server_komodo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        
        // Configure JSON serializer for values
        Jackson2JsonRedisSerializer<Object> valueSerializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        
        RedisSerializationContext<String, Object> serializationContext = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(new StringRedisSerializer())
                .value(valueSerializer)
                .hashKey(new StringRedisSerializer())
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}