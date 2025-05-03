// Security Configuration Class
package kjistik.auth_server_komodo.Config;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.security.Keys;

@Configuration
public class SecretKeyProvider {

    private final JwtConfig jwtProperties;

    public SecretKeyProvider(JwtConfig jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}