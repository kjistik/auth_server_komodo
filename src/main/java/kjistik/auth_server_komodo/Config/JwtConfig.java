package kjistik.auth_server_komodo.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {  // Renamed to avoid "Config" confusion
    private String secretKey;
    private long expirationTime;
    private long verificationExpirationTime;
    private long refreshExpirationTime;
}

