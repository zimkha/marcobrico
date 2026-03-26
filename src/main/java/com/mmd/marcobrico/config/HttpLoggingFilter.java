package com.mmd.marcobrico.config;

import com.mmd.marcobrico.service.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Order(1)
@RequiredArgsConstructor
public class HttpLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest =
            new ContentCachingRequestWrapper(request, 1);
        ContentCachingResponseWrapper wrappedResponse =
            new ContentCachingResponseWrapper(response);
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());

        // extraction du User
        String userId = extractUserFromToken(request);
        if (userId != null) {
            MDC.put("userId", userId);
        }
        response.setHeader("X-CorrelationID", correlationId);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        }finally {
            long duration = System.currentTimeMillis() - startTime;
            String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.info("HTTP_REQUEST",
                kv("correlationId", correlationId),
                kv("method", request.getMethod()),
                kv("uri", request.getRequestURI()),
                kv("queryString", request.getQueryString()),
                kv("status", wrappedResponse.getStatus()),
                kv("durationMs", duration),
                kv("userId", userId),
                kv("userAgent", request.getHeader("User-Agent")),
                kv("requestBody", sanitize(requestBody)),
                kv("responseBody", truncate(responseBody))
            );

            MDC.put("status", String.valueOf(wrappedResponse.getStatus()));
            MDC.put("duration", String.valueOf(duration));
            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }
    private String extractUserFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            return jwtUtils.getUsernameFromJwtToken(token);
        } catch (Exception e) {
            return null;
        }
    }
    private String sanitize(String body) {
        if (body == null || body.isBlank()) return null;

        return body
            .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
            .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
    }

    private String truncate(String body) {
        if (body == null || body.isBlank()) return null;
        return body.length() > 1000 ? body.substring(0, 1000) + "...[truncated]" : body;
    }
}
