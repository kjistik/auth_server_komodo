package kjistik.auth_server_komodo.Security;

import java.security.PublicKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kjistik.Validator.JwtValidator;
import reactor.core.publisher.Mono;

@Configuration
public class JwtValidatorProvider {
    @Bean
    public JwtValidator jwtValidator(@Qualifier("vaultPublicKey") Mono<PublicKey> publicKey) {
        return new JwtValidator(publicKey.block());
    }
}
