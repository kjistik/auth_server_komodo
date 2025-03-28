package kjistik.auth_server_komodo.Exceptions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<Class<?>, ErrorSpec> ERROR_MAPPINGS = Map.ofEntries(
            Map.entry(JwtAuthenticationException.class,
                    new ErrorSpec(HttpStatus.UNAUTHORIZED, "INVALID_JWT", true,
                            headers -> headers.add("WWW-Authenticate", "Bearer error=\"invalid_token\""))),

            Map.entry(ExpiredJWTException.class,
                    new ErrorSpec(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", true,
                            headers -> headers.add("WWW-Authenticate", "Bearer error=\"expired_token\""))),

            Map.entry(MissingSessionCookieException.class,
                    new ErrorSpec(HttpStatus.UNAUTHORIZED, "MISSING_SESSION", true,
                            headers -> headers.add("Set-Cookie",
                                    "SESSION_ID=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=Strict"))),

            Map.entry(InvalidFingerprintsException.class,
                    new ErrorSpec(HttpStatus.FORBIDDEN, "DEVICE_MISMATCH", false,
                            headers -> headers.add("X-Security-Alert", "true"))),

            Map.entry(InvalidCredentialsException.class,
                    new ErrorSpec(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", true)),

            Map.entry(RepeatedUserNameException.class,
                    new ErrorSpec(HttpStatus.CONFLICT, "USERNAME_EXISTS", false)),

            Map.entry(UserNotFoundException.class,
                    new ErrorSpec(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", false)),

            Map.entry(EmailNotSentException.class,
                    new ErrorSpec(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_FAILURE", false)));

    record ErrorSpec(
            HttpStatus status,
            String code,
            boolean logAsWarning,
            Consumer<HttpHeaders> headerConfig) {
        ErrorSpec(HttpStatus status, String code, boolean logAsWarning) {
            this(status, code, logAsWarning, null);
        }
    }

    @ExceptionHandler
    public Mono<ResponseEntity<Map<String, Object>>> handle(Exception ex, ServerWebExchange exchange) {
        ErrorSpec spec = ERROR_MAPPINGS.getOrDefault(ex.getClass(),
                new ErrorSpec(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", false));

        logSecurityEvent(ex, exchange, spec);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        if (spec.headerConfig() != null) {
            spec.headerConfig().accept(headers);
        }

        return Mono.just(ResponseEntity.status(spec.status())
                .headers(headers)
                .body(createErrorBody(ex, spec)));
    }

    private void logSecurityEvent(Exception ex, ServerWebExchange exchange, ErrorSpec spec) {
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        String message = String.format("%s | %s | %s",
                spec.code(),
                ip,
                exchange.getRequest().getPath().value());

        if (spec.logAsWarning()) {
            log.warn(message);
        } else {
            log.error(message + " | " + ex.getMessage());
        }
    }

    private Map<String, Object> createErrorBody(Exception ex, ErrorSpec spec) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getMessage());
        body.put("code", spec.code());
        body.put("timestamp", Instant.now());
        return body;
    }
}