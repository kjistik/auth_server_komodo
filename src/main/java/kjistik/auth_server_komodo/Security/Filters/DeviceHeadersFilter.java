package kjistik.auth_server_komodo.Security.Filters;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class DeviceHeadersFilter implements WebFilter {

    private static final List<String> REQUIRED_HEADERS = List.of(
            "User-Agent",
            "X-Timezone",
            "X-OS",
            "X-Resolution");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getURI().getPath().equals("/auth/login")) {
            for (String header : REQUIRED_HEADERS) {
                if (request.getHeaders().getFirst(header) == null) {
                    System.out.println("Faltan headers");
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse()
                            .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                                    .wrap(("Missing header: " + header).getBytes())));
                }
            }
        }
        return chain.filter(exchange);
    }
}