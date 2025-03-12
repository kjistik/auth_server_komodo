package kjistik.auth_server_komodo.Config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import kjistik.auth_server_komodo.Utils.JwtUtils;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements WebFilter {

    @Autowired
    JwtUtils utils;

    private static final String[] IGNORED_PATHS = { "/auth/login",
            "/auth/error",
            "/auth/register",
            "/auth/verify", "/test" };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Check if the request path should be ignored
        String path = exchange.getRequest().getPath().value();
        for (String ignoredPath : IGNORED_PATHS) {
            if (ignoredPath.equals(path)) {
                return chain.filter(exchange);
            }
        }

        // Extract the Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        // Check if the Authorization header is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeUnauthorizedResponse(exchange, "Missing or invalid JWT");
        }

        String token = authHeader.substring(7);

        try {
            // Validate the JWT token
            Jws<Claims> claims = utils.validateToken(token);
            if (claims != null) {
                String username = claims.getPayload().getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = claims.getPayload().get("roles", List.class);

                // Map roles to GrantedAuthority objects
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Prefix with "ROLE_"
                        .collect(Collectors.toList());

                // Create an Authentication object
                User user = new User(username, "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

                // Set the authentication in the SecurityContext
                SecurityContext securityContext = new SecurityContextImpl(authentication);
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            }
        } catch (JwtException e) {
            // Handle invalid or expired JWT
            return writeUnauthorizedResponse(exchange, "Invalid or expired JWT");
        }

        // If the token is valid, proceed with the filter chain
        return chain.filter(exchange);
    }

    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String errorMessage) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String responseBody = "{\"error\": \"" + errorMessage + "\"}";
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(responseBody.getBytes())));
    }
}