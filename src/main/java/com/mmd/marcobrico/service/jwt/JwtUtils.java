package com.mmd.marcobrico.service.jwt;

import com.mmd.marcobrico.config.AppJwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;



@Component
public class JwtUtils {


    private final AppJwtConfig jwtConfig;
    private final SecretKey secretKey;
    public JwtUtils(AppJwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;

        // Génère la clé UNE SEULE FOIS au démarrage
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        System.out.println("=== JWT CONFIG LOADED ===");
        System.out.println("Secret: " + jwtConfig.getSecret());
        System.out.println("Secret length: " + jwtConfig.getSecret().length() + " chars");
        System.out.println("Secret bytes length: " + keyBytes.length);
        System.out.println("Secret hashCode: " + Arrays.hashCode(keyBytes));
        System.out.println("Expiration: " + jwtConfig.getExpiration());
        System.out.println("SecretKey generated and cached");
    }

    private SecretKey key() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        int hashCode = Arrays.hashCode(keyBytes);

        System.out.println("[KEY] Secret: " + (jwtConfig.getSecret() != null ? jwtConfig.getSecret() : "NULL"));
        System.out.println("[KEY] Secret length: " + (jwtConfig.getSecret() != null ? jwtConfig.getSecret().length() : 0));
        System.out.println("[KEY] Key bytes hashCode: " + hashCode);

        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(UserDetails userDetails) {
        System.out.println("\n=== GENERATING TOKEN ===");
        System.out.println("Username: " + userDetails.getUsername());
        System.out.println("Authorities: " + userDetails.getAuthorities());

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("Token generated successfully");
        return token;
    }

    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    public String getRoleFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    public boolean validateJwtToken(String token) {
        System.out.println("\n=== VALIDATING TOKEN ===");
        System.out.println("Token to validate: " + token);
        System.out.println("Token length: " + token.length());
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            System.out.println("✅ Token validation SUCCESS");
            return true;

        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }

        return false;
    }


}

