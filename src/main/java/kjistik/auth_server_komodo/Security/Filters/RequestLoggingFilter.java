package kjistik.auth_server_komodo.Security.Filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getPath().toString();

        return chain.filter(exchange).doFinally(signal -> {
            String logMsg = String.format(
                "%s %s - %dms",
                exchange.getRequest().getMethod(),
                path,
                System.currentTimeMillis() - startTime
            );
            
            // Special logging for security endpoints
            if (path.startsWith("/auth")) {
                log.info("SECURITY REQUEST: {}", logMsg);
            } else {
                log.debug(logMsg);
            }
        });
    }
}