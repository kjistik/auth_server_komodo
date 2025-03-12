package kjistik.auth_server_komodo.Config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
                // Get the authenticated user details
                UserDetails user = (UserDetails) authentication.getPrincipal();
                String username = user.getUsername().toLowerCase();

                // Check if the user is verified
                return userService.isUserVerified(username)
                                // If the user is verified, proceed with generating and writing the token
                                .then(generateAndWriteToken(webFilterExchange, user));
        }

        private Mono<Void> generateAndWriteToken(WebFilterExchange webFilterExchange, UserDetails user) {
                // Extract roles from the authenticated user
                List<String> roles = user.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .collect(Collectors.toList());
            
                // Generate JWT token
                Mono<String> tokenMono = utils.generateJwtToken(user.getUsername(), roles);
            
                // Write the token to the response
                ServerWebExchange exchange = webFilterExchange.getExchange();
            
                return tokenMono.flatMap(token -> {
                    // Create response body with the actual token value
                    String responseBody = "{\"token\": \"" + token + "\"}";
            
                    // Write the response body to the response
                    return exchange.getResponse()
                            .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                                    .wrap(responseBody.getBytes())));
                });
            }
            
}