package net.yosifov.accounting.accj.accj2gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

// https://www.programcreek.com/java-api-examples/?api=org.springframework.cloud.gateway.filter.GlobalFilter
@Component
public class SecurityFilter implements GlobalFilter {

    private Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Value("${application.jwt.tokenPrefix}")
    private String tokenPrefix;

    @Autowired
    private SecretKey appSecretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
        logger.info("Path of the request received -> {}",
                exchange.getRequest().getPath());
        ServerHttpRequest request = exchange.getRequest();
        System.out.println(request);
        RequestPath path = request.getPath();
        System.out.println(path);
        if (path.toString().equals("/get") && false) {
            throw new RuntimeException("Bla bla bla");
        }
        if(isBad(exchange)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }


        return chain.filter(exchange);
    }

    private boolean isBad(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        HttpHeaders httpHeaders = request.getHeaders();
        String token = getToken(httpHeaders);



        return false;
    }

    private String getToken(HttpHeaders httpHeaders) {
        List<String> authHeaderList = httpHeaders.get("Authorization");
        if(null == authHeaderList) {
            return null;
        }
        String bearer = authHeaderList.get(0);
        String token = bearer.replace(tokenPrefix, "").trim();

        return token;
    }

}