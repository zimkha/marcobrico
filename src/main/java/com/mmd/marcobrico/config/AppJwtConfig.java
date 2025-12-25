package com.mmd.marcobrico.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "jwt")
@Configuration
@Getter
@Setter
public class AppJwtConfig {

    private String secret;
    private Long expiration;
}
