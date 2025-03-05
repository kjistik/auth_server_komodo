package kjistik.auth_server_komodo.Exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private final Map<Class<? extends Throwable>, HttpStatus> exceptionStatusMap = new HashMap<>();

    public GlobalWebExceptionHandler() {
        exceptionStatusMap.put(UserNotVerifiedException.class, HttpStatus.UNAUTHORIZED);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        String errorMessage = ex.getMessage();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorBody = "{\"error\": \"" + errorMessage + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorBody.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}