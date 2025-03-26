package kjistik.auth_server_komodo.Services.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import kjistik.auth_server_komodo.Config.AuthenticationHandler;
import kjistik.auth_server_komodo.Exceptions.InvalidCredentialsException;
import kjistik.auth_server_komodo.Security.CustomUserDetailsService;
import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import kjistik.auth_server_komodo.Utils.RequestEntities.LoginRequest;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    RefreshTokenService refreshService;

    public AuthService(CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder,
            AuthenticationHandler authenticationHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationHandler = authenticationHandler;
    }

    public Mono<Void> login(LoginRequest loginRequest, ServerWebExchange exchange, String agent, String os,
            String resolution, String timezone) {
        return customUserDetailsService.findByUsername(loginRequest.getUsername())
                .flatMap(userDetails -> {
                    // Check if the password matches
                    if (passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
                        // Create an Authentication object
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        // Trigger the authentication success handler
                        return authenticationHandler.onAuthenticationSuccess(
                                new WebFilterExchange(exchange, chain -> Mono.empty()), authentication, agent, os,
                                resolution, timezone);
                    } else {
                        return Mono.error(new InvalidCredentialsException("Invalid credentials"));
                    }
                })
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")));
    }
}