package kjistik.auth_server_komodo.Config;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import kjistik.auth_server_komodo.Services.User.UserService;
import kjistik.auth_server_komodo.Utils.JwtUtils;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationHandler implements ServerAuthenticationSuccessHandler {

        @Autowired
        JwtUtils utils;

        @Autowired
        UserService userService;

        @Override
        public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
                UserDetails user = (UserDetails) authentication.getPrincipal();
                String username = user.getUsername().toLowerCase();

                return userService.isUserVerified(username)
                                .then(generateAndWriteToken(webFilterExchange, user));
        }

        private Mono<Void> generateAndWriteToken(WebFilterExchange webFilterExchange, UserDetails user) {
                List<String> roles = user.getAuthorities().stream()
                                .map(grantedAuthority -> grantedAuthority.getAuthority())
                                .collect(Collectors.toList());

                String session = UUID.randomUUID().toString();
                // Generate session ID and JWT token together
                return utils.generateJwtToken(user.getUsername(), session, roles)
                                .flatMap(jwtResponse -> {
                                        // Create cookie with session ID
                                        ResponseCookie sessionCookie = ResponseCookie
                                                        .from("SESSION_ID", jwtResponse.getSessionId())
                                                        .httpOnly(true)
                                                        .secure(true)
                                                        .path("/")
                                                        .maxAge(Duration.ofDays(30))
                                                        .build();

                                        // Create response body
                                        String responseBody = String.format(
                                                        "{\"token\": \"%s\"}",
                                                        jwtResponse.getToken());

                                        // Write response
                                        ServerWebExchange exchange = webFilterExchange.getExchange();
                                        exchange.getResponse().addCookie(sessionCookie);

                                        return exchange.getResponse()
                                                        .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                                                                        .wrap(responseBody.getBytes())));
                                });
        }
}