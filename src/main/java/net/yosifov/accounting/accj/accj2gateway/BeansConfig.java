package net.yosifov.accounting.accj.accj2gateway;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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
}
