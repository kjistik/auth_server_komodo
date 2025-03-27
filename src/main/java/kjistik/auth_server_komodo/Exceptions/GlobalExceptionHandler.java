package kjistik.auth_server_komodo.Exceptions;

import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;
import reactor.core.publisher.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Map<Class<?>, ErrorSpec> ERROR_MAPPINGS = Map.of(
        JwtAuthenticationException.class, new ErrorSpec(HttpStatus.UNAUTHORIZED, "INVALID_JWT", true),
        ExpiredJWTException.class, new ErrorSpec(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", true),
        MissingSessionCookieException.class, new ErrorSpec(HttpStatus.UNAUTHORIZED, "MISSING_SESSION", true),
        InvalidFingerprintsException.class, new ErrorSpec(HttpStatus.FORBIDDEN, "DEVICE_MISMATCH", false),
        InvalidCredentialsException.class, new ErrorSpec(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", true),
        RepeatedUserNameException.class, new ErrorSpec(HttpStatus.CONFLICT, "USERNAME_EXISTS", false),
        UserNotFoundException.class, new ErrorSpec(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", false),
        EmailNotSentException.class, new ErrorSpec(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_FAILURE", false)
    );

    record ErrorSpec(HttpStatus status, String code, boolean logWarning) {
        void log(Exception ex, ServerWebExchange exg) {
            String ip = exg.getRequest().getRemoteAddress() != null ? 
                exg.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            if (logWarning) log.warn("{} from {}: {}", code, ip, ex.getMessage());
            else log.error("{} from {}: {}", code, ip, ex.getMessage());
        }
    }

    @ExceptionHandler
    public Mono<ResponseEntity<Map<String, Object>>> handle(Exception ex, ServerWebExchange exg) {
        ErrorSpec spec = ERROR_MAPPINGS.getOrDefault(ex.getClass(), 
            new ErrorSpec(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", false));
        
        spec.log(ex, exg);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        if (ex instanceof ExpiredJWTException) 
            headers.add("WWW-Authenticate", "Bearer error=\"invalid_token\"");
        if (ex instanceof MissingSessionCookieException) 
            headers.add("Set-Cookie", "SESSION_ID=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=Strict");

        return Mono.just(ResponseEntity.status(spec.status())
            .headers(headers)
            .body(Map.of(
                "error", ex.getMessage(),
                "code", spec.code(),
                "timestamp", Instant.now()
            )));
    }
}