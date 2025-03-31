package kjistik.auth_server_komodo.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalWebExceptionHandler.class);

    private final Map<Class<? extends Throwable>, HttpStatus> exceptionStatusMap = new HashMap<>();
    private final Map<Class<? extends Throwable>, String> errorCodeMap = new HashMap<>();

    public GlobalWebExceptionHandler() {
        // Existing exceptions
        exceptionStatusMap.put(UserNotVerifiedException.class, HttpStatus.FORBIDDEN);
        errorCodeMap.put(UserNotVerifiedException.class, "USER_NOT_VERIFIED");

        exceptionStatusMap.put(JwtAuthenticationException.class, HttpStatus.UNAUTHORIZED);
        errorCodeMap.put(JwtAuthenticationException.class, "JWT_AUTH_ERROR");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        String errorCode = errorCodeMap.getOrDefault(ex.getClass(), "INTERNAL_ERROR");

        // Log the error
        log.error("{}: {} - Path: {}",
                errorCode,
                ex.getMessage(),
                exchange.getRequest().getPath(),
                ex); // Include stack trace for errors

        // Prepare response
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");

        // Create consistent error format
        String errorBody = String.format(
                "{\"error\": \"%s\", \"code\": \"%s\", \"timestamp\": \"%s\"}",
                ex.getMessage(),
                errorCode,
                Instant.now());

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(errorBody.getBytes())));
    }
}