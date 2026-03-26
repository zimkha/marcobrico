package com.mmd.marcobrico.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppJwtConfig {

    private Jwt jwt;
    private Front front;

    public AppJwtConfig(Jwt jwt, Front front) {
        this.jwt = jwt;
        this.front = front;
    }
    public record Jwt(String secret, Long expiration) {}
    public record Front(String endpoint) {}
}
