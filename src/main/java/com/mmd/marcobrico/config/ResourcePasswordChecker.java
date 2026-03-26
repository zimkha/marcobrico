package com.mmd.marcobrico.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ResourcePasswordChecker implements CompromisedPasswordChecker {

    private final Resource resource;
    private Set<String> compromisedPasswords = Set.of();

    public ResourcePasswordChecker(Resource resource) {
        this.resource = resource;
    }

    @PostConstruct
    void init() {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            compromisedPasswords = reader.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());

            log.info("Responsible for {} compromised passwords", compromisedPasswords.size());

        } catch (IOException e) {
            log.error("Unable to load the password file", e);
            compromisedPasswords = Set.of();
        }
    }

    @Override
    public CompromisedPasswordDecision check(@Nullable String password) {
        if (password == null || password.isBlank()) {
            return new CompromisedPasswordDecision(false);
        }

        boolean compromised = compromisedPasswords.contains(password.trim());

        if (compromised) {
            log.debug("Compromise password detected");
        }

        return new CompromisedPasswordDecision(compromised);
    }
}

