package kjistik.auth_server_komodo.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Component
@Getter
@Setter
public class JwtConfig {

    private String secretKey;
    private long expirationTime;
    private long verificationExpirationTime;
    private long refreshExpirationTime;

}