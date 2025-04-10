package kjistik.auth_server_komodo.Services.User;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import kjistik.auth_server_komodo.Config.AuthenticationHandler;
import kjistik.auth_server_komodo.Exceptions.ExpiredJWTException;
import kjistik.auth_server_komodo.Exceptions.InvalidCredentialsException;
import kjistik.auth_server_komodo.Exceptions.InvalidFingerprintsException;
import kjistik.auth_server_komodo.Exceptions.JwtAuthenticationException;
import kjistik.auth_server_komodo.Security.CustomUserDetailsService;
import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import kjistik.auth_server_komodo.Utils.DeviceFingerprintUtils;
import kjistik.auth_server_komodo.Utils.JwtUtils;
import kjistik.auth_server_komodo.Utils.RequestEntities.LoginRequest;
import kjistik.auth_server_komodo.Utils.RequestEntities.TokenResponse;
import reactor.core.publisher.Mono;

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

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
    log.warn("Reissue attempt with missing session cookie");
    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing session cookie"));
}

try {
    log.debug("Starting token reissue validation for session: {}", sessionId);
    
    // 1. Validate token (passes if either valid or expired)
    Claims claims = utils.validateTokenToleratingExpired(jwtToken).getPayload();
    log.debug("Token validation passed for user: {}", claims.getSubject());
    
    // 2. SINGLE expiration check
    Date gracePeriodCutoff = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
    Date expiration = claims.getExpiration();
    
    log.debug("Token expiration check - Expires: {}, Grace cutoff: {}, Current: {}", 
        expiration, gracePeriodCutoff, new Date());
    
    if (expiration.before(gracePeriodCutoff)) {
        log.warn("Token expired beyond grace period for user: {}", claims.getSubject());
        throw new ExpiredJWTException();
    }
    
    // 3. Proceed with reissue (valid OR expired ≤7 days)
    String username = claims.getSubject();
    log.debug("Proceeding with token reissue for user: {}", username);
    
    List<String> roles = utils.extractRolesFromExpiredToken(jwtToken);
    TokenResponse response = new TokenResponse();
    
    return refreshService.getRefreshToken(username, sessionId)
        .doOnNext(payload -> log.debug("Refresh token found for user: {}", username))
        .flatMap(payload -> DeviceFingerprintUtils.generateFingerprint(agent, timezone, os, resolution)
            .doOnNext(fingerprint -> log.debug("Generated fingerprint for device check"))
            .flatMap(fingerprint -> {
                if (fingerprint.equals(payload.getFingerprint())) {
                    log.debug("Device fingerprint match for user: {}", username);
                    return utils.generateJwtToken(username, sessionId, roles, agent, os, resolution, timezone)
                        .map(responseToken -> {
                            response.setToken(responseToken.getToken());
                            log.info("Successfully reissued token for user: {}", username);
                            return response;
                        });
                }
                log.warn("Device fingerprint mismatch for user: {}", username);
                return service.sendSuspiciousActivityEmail(username, agent, os)
                    .then(refreshService.deleteRefreshToken(username, sessionId))
                    .then(Mono.error(new InvalidFingerprintsException()));
            }));
            
} catch (JwtAuthenticationException e) {
    log.warn("Invalid token in reissue attempt: {}", e.getMessage());
    return Mono.error(e);
} catch (ExpiredJWTException e) {
    log.warn("Token rejected - expired beyond grace period");
    return Mono.error(e);
} catch (Exception e) {
    log.error("Unexpected error during token reissue", e);
    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token reissue failed"));
}
}}