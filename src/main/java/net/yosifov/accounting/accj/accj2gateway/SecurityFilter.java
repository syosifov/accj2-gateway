package net.yosifov.accounting.accj.accj2gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        RequestPath path = request.getPath();
        String sPath = path.toString();

        if (sPath.equals("/get") && false) {
            throw new RuntimeException("Bla bla bla");
        }
        if(sPath.equals("/api/v1/auth/login")) {
            return chain.filter(exchange);
        }
        if(sPath.equals("/api/v1/auth/refresh")) {
            return chain.filter(exchange);
        }
        if(sPath.equals("/api/v1/auth/testRequest")) {
            return chain.filter(exchange);
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
        if(null == token || token.trim().isEmpty()) {
            return true;
        }

        try {
            Jws<Claims> claimsJws = Jwts
                    .parserBuilder()
                    .setSigningKey(appSecretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = claimsJws.getBody();

            String username = claims.getSubject();
            Long id = claims.get("id", Long.class);

            Map<String,Object> principalDataMap = new HashMap<>();
            principalDataMap.put("username", username);
            principalDataMap.put("id", id);

            var authorities = (List<Map<String, Object>>) claims.get("authorities");

//            Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
//                    .map(m -> new SimpleGrantedAuthority((m.get("authority")).toString()))
//                    .collect(Collectors.toSet());
//
//            Authentication authentication = new UsernamePasswordAuthenticationToken(
//                    principalDataMap,
//                    null,
//                    simpleGrantedAuthorities
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            throw new IllegalStateException(String.format("Token cannot be trusted: %s", token));
        }


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