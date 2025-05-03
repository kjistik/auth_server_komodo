package kjistik.auth_server_komodo.Security;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kjistik.Validator.JwtValidator;

@Configuration
public class JwtValidatorProvider {
    @Bean
    public JwtValidator jwtValidator(SecretKey secretKey) {
        return new JwtValidator(secretKey);
    }
}
