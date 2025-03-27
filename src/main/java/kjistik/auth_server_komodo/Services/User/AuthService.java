package kjistik.auth_server_komodo.Services.User;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import kjistik.auth_server_komodo.Config.AuthenticationHandler;
import kjistik.auth_server_komodo.Exceptions.InvalidCredentialsException;
import kjistik.auth_server_komodo.Security.CustomUserDetailsService;
import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import kjistik.auth_server_komodo.Utils.JwtUtils;
import kjistik.auth_server_komodo.Utils.RequestEntities.LoginRequest;
import kjistik.auth_server_komodo.Utils.RequestEntities.TokenResponse;
import reactor.core.publisher.Mono;
import kjistik.auth_server_komodo.Utils.DeviceFingerprintUtils;

@Service
public class AuthService {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    RefreshTokenService refreshService;

    @Autowired
    JwtUtils utils;

    @Autowired
    UserService service;

    public AuthService(CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder,
            AuthenticationHandler authenticationHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationHandler = authenticationHandler;
    }

    public Mono<Boolean> endSession(String username, String sessionId) {
        return refreshService.deleteRefreshToken(username, sessionId);
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

    public Mono<TokenResponse> reIssueToken(String agent, String os,
            String resolution, String timezone, String sessionId, String jwtToken) {

        if (sessionId == null || sessionId.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing session cookie"));
        }

        // 1. Validate token with grace period
        Claims claims;
        try {
            claims = utils.validateToken(jwtToken).getPayload();

            // Manual expiration check with grace period (1 week)
            if (claims.getExpiration().before(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)))) {
                throw new JwtException("Token expired beyond grace period");
            }
        } catch (JwtException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token: " + e.getMessage()));
        }
        // 2. Extract user info
        String username = claims.getSubject();
        List<String> roles = utils.extractRolesFromToken(jwtToken);
        TokenResponse response = new TokenResponse();
        return refreshService.getRefreshToken(username, sessionId)
                .flatMap(payload -> DeviceFingerprintUtils.generateFingerprint(agent, timezone, os, resolution)
                        .flatMap(fingerprint -> {
                            if (fingerprint.equals(payload.getFingerprint())) {
                                return utils
                                        .generateJwtToken(username, sessionId, roles, agent, os, resolution, timezone)
                                        .map(responseToken -> {
                                            response.setToken(responseToken.getToken());
                                            return response;
                                        });
                            }
                            return service.sendSuspiciousActivityEmail(username, agent, os).then(
                                    Mono.error(new ResponseStatusException(
                                            HttpStatus.FORBIDDEN,
                                            "Device fingerprint mismatch")));
                        }));
    }
}