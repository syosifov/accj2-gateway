package net.yosifov.accounting.accj.accj2gateway;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Configuration
public class BeansConfig {

    @Value("${application.jwt.secretKeyString}")
    private String secretKeyString;

    @Bean
    public SecretKey appSecretKey() throws UnsupportedEncodingException {
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        return secretKey;
    }

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {


        RouteLocator build = builder.routes()
                .route(p -> p.path("/get")
                        .filters(f -> f
                                .addRequestHeader("MyHeader", "MyUri")
                                .addRequestParameter("myParam", "MyValue"))
                        .uri("http://httpbin.org:80"))
                .route(p -> p.path("/currency-exchange/**")
                        .uri("lb://currency-exchange"))
                .route(p -> p.path("/currency-conversion/**")
                        .uri("lb://currency-conversion"))
                .route(p -> p.path("/currency-conversion-feign/**")
                        .uri("lb://currency-conversion"))
                .route(p -> p.path("/currency-conversion-new/**")
                        .filters(f -> f.rewritePath(
                                "/currency-conversion-new/(?<segment>.*)",
                                "/currency-conversion-feign/${segment}"))
                        .uri("lb://currency-conversion"))
                .build();

        RouteLocator routeLocator = builder.routes()
                .route(p -> p.path("/api/v1/auth/login")
                        .uri("lb://accj2-login"))
                .route(p -> p.path("/api/v1/auth/refresh")
                        .uri("lb://accj2-login"))
                .route(p -> p.path("/api/v1/accounts/**")
                        .uri("lb://accj2"))

                .build();

        return routeLocator;
    }
}
