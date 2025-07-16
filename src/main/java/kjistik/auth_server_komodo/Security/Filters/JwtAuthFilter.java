package kjistik.auth_server_komodo.Security.Filters;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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
import kjistik.Validator.JwtValidator;
import kjistik.auth_server_komodo.Exceptions.JwtAuthenticationException;
import kjistik.auth_server_komodo.Utils.IgnoredEndpoint;
import kjistik.auth_server_komodo.Utils.JwtUtils;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements WebFilter {

    @Autowired
    JwtUtils utils;

    JwtValidator validator;

    public JwtAuthFilter(JwtValidator validator) {
        this.validator = validator;
    }

    private final List<IgnoredEndpoint> IGNORED_ENDPOINTS = List.of(
            new IgnoredEndpoint("/auth/error", null),
            new IgnoredEndpoint("/auth/api/user", HttpMethod.POST.name()),
            new IgnoredEndpoint("/auth/verify", null),
            new IgnoredEndpoint("/test", null),
            new IgnoredEndpoint("/auth/reissue", null),
            new IgnoredEndpoint("/auth/login", HttpMethod.POST.name()));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Check if the request path should be ignored
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        for (IgnoredEndpoint ignored : IGNORED_ENDPOINTS) {
            if (ignored.getPath().equals(path)) {
                if (ignored.getMethod() == null || ignored.getMethod().equals(method)) {
                    return chain.filter(exchange);
                }
            }
        }

        // Extract the Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        // Check if the Authorization header is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new JwtAuthenticationException("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        try {
            // Validate the JWT token
            Jws<Claims> claims = validator.validateToken(token);
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
            return Mono.error(new JwtAuthenticationException("JWT validation failed: " + e.getMessage(), e));
        }

        // If the token is valid, proceed with the filter chain
        return chain.filter(exchange);
    }

}