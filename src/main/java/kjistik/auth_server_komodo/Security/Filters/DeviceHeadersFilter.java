package kjistik.auth_server_komodo.Security.Filters;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import kjistik.auth_server_komodo.Exceptions.MissingDeviceHeaderException;
import reactor.core.publisher.Mono;

@Component
public class DeviceHeadersFilter implements WebFilter {

    private static final List<String> REQUIRED_HEADERS = List.of(
            "User-Agent",
            "X-Timezone",
            "X-OS",
            "X-Resolution");

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.fromCallable(() -> {
            ServerHttpRequest request = exchange.getRequest();

            if (request.getURI().getPath().equals("/auth/login")) {
                REQUIRED_HEADERS.forEach(header -> {
                    if (request.getHeaders().getFirst(header) == null) {
                        throw new MissingDeviceHeaderException(header);
                    }
                });
            }
            return chain.filter(exchange);
        })
                .onErrorResume(MissingDeviceHeaderException.class, e -> Mono.error(e))
                .flatMap(mono -> mono);
    }

}