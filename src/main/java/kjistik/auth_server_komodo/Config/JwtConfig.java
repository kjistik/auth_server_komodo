package kjistik.auth_server_komodo.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtConfig {

    private String secretKey;
    private long expirationTime;
    private long verificationExpirationTime;

    // Getters and Setters
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setVerificationExpirationTime(long verificationExpirationTime) {
        this.verificationExpirationTime = verificationExpirationTime;
    }

    public long getVerificationExpirationTime() {
        return verificationExpirationTime;
    }
}