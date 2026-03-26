# Spring Security 7 - Guide Complet (Partie 2)

## Suite de UserPrincipal

```java
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
        
        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }
    
    public static UserPrincipal create(
            User user,
            Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }
    
    // UserDetails methods
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public String getName() {
        return String.valueOf(id);
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
}
```

#### E) OAuth2 Resource Server (API protégée)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Configuration :**

```java
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        
        // Extrait les rôles du claim "roles" au lieu de "scope"
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter);
        
        return jwtAuthenticationConverter;
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        // Utilise la même clé que le Authorization Server
        SecretKey key = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtSecret));
        
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

**application.yml pour Resource Server :**

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # URL du Authorization Server (JWKS endpoint)
          jwk-set-uri: https://auth.example.com/.well-known/jwks.json
          # OU secret key si symmetric
          # secret: ${JWT_SECRET}
          issuer-uri: https://auth.example.com
```

---

## 8. Architecture Microservices avec Spring Security

### 8.1 Architecture Globale

```
┌─────────────────────────────────────────────────────────────┐
│                         API Gateway                          │
│              (Spring Cloud Gateway + Security)               │
│  - Authentification centralisée                             │
│  - Routing                                                  │
│  - Rate Limiting                                            │
└──────────┬─────────────────────────────────────┬────────────┘
           │                                     │
           │                                     │
┌──────────▼──────────┐              ┌──────────▼──────────┐
│  Auth Service       │              │  Business Services  │
│  (Keycloak/Custom)  │              │  (Resource Servers) │
│  - Login            │              │  - User Service     │
│  - Token Generation │              │  - Order Service    │
│  - User Management  │              │  - Product Service  │
└─────────────────────┘              └─────────────────────┘
```

### 8.2 Service d'Authentification (Auth Service)

#### A) Structure du Projet

```
auth-service/
├── src/main/java/com/example/auth/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   └── CorsConfig.java
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JwtTokenProvider.java
│   │   └── RefreshTokenService.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── RefreshToken.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── RefreshTokenRepository.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── JwtAuthenticationResponse.java
└── src/main/resources/
    └── application.yml
```

#### B) Configuration de Sécurité

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
```

#### C) Service d'Authentification

```java
@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    
    /**
     * Inscription d'un nouvel utilisateur
     */
    public UserDto register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                "Email already in use: " + request.getEmail());
        }
        
        // Vérifier si le username existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                "Username already taken: " + request.getUsername());
        }
        
        // Créer le nouvel utilisateur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        
        // Assigner le rôle USER par défaut
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("User Role not found"));
        user.setRoles(Collections.singleton(userRole));
        
        User savedUser = userRepository.save(user);
        
        // Envoyer email de confirmation (asynchrone)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        
        return UserDto.fromUser(savedUser);
    }
    
    /**
     * Connexion et génération de tokens
     */
    public JwtAuthenticationResponse login(LoginRequest request) {
        // Authentifier
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Générer tokens
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        // Sauvegarder refresh token
        refreshTokenService.saveRefreshToken(
            request.getUsername(),
            refreshToken,
            request.getDeviceInfo()
        );
        
        // Récupérer l'utilisateur pour les infos supplémentaires
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));
        
        return JwtAuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getAccessTokenExpirationMs())
            .user(UserDto.fromUser(user))
            .build();
    }
    
    /**
     * Rafraîchir le access token
     */
    public JwtAuthenticationResponse refreshAccessToken(String refreshToken) {
        // Valider refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        
        // Vérifier en base
        if (!refreshTokenService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }
        
        // Extraire username
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        
        // Charger utilisateur
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        
        UserDetails userDetails = CustomUserDetails.fromUser(user);
        
        // Créer authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        // Générer nouveau access token
        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        
        // Optionnel : rotation du refresh token
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
        refreshTokenService.rotateRefreshToken(refreshToken, newRefreshToken);
        
        return JwtAuthenticationResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getAccessTokenExpirationMs())
            .user(UserDto.fromUser(user))
            .build();
    }
    
    /**
     * Déconnexion
     */
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
    
    /**
     * Déconnexion de tous les appareils
     */
    public void logoutAllDevices(String username) {
        refreshTokenService.revokeAllUserTokens(username);
    }
}
```

#### D) DTOs

```java
@Data
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String deviceInfo;
}

@Data
@Builder
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain uppercase, lowercase, digit and special character"
    )
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDto user;
}

@Data
@Builder
public class UserDto {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private boolean enabled;
    private Instant createdAt;
    
    public static UserDto fromUser(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .enabled(user.isEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
```

### 8.3 API Gateway avec Spring Cloud Gateway

#### A) Dépendances

```xml
<dependencies>
    <!-- Spring Cloud Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    
    <!-- Service Discovery (Eureka) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Circuit Breaker (Resilience4j) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
    </dependency>
</dependencies>
```

#### B) Configuration Gateway

```yaml
# application.yml
spring:
  application:
    name: api-gateway
  
  cloud:
    gateway:
      # Configuration globale
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
        - name: CircuitBreaker
          args:
            name: defaultCircuitBreaker
            fallbackUri: forward:/fallback
        - name: RequestRateLimiter
          args:
            redis-rate-limiter:
              replenishRate: 10
              burstCapacity: 20
      
      # Routes
      routes:
        # Auth Service
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1
        
        # User Service (protégé)
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: AuthenticationFilter
        
        # Order Service (protégé)
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
            - name: AuthenticationFilter
        
        # Product Service (partiellement protégé)
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=1
      
      # CORS global
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:3000"
              - "https://app.example.com"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders:
              - "*"
            allowCredentials: true
            maxAge: 3600

  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://auth-service/oauth2/jwks
          issuer-uri: http://auth-service

# Eureka Client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

#### C) Security Configuration Gateway

```java
@Configuration
@EnableWebFluxSecurity  // WebFlux pour Gateway (pas WebMvc)
public class GatewaySecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http) {
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Routes publiques
                .pathMatchers(
                    "/api/auth/**",
                    "/api/products/public/**",
                    "/actuator/health"
                ).permitAll()
                
                // Routes admin
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Autres routes : authentification requise
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") 
            String jwkSetUri) {
        return ReactiveJwtDecoders.fromIssuerLocation(jwkSetUri);
    }
    
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> 
            jwtAuthenticationConverter() {
        
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter);
        
        return new ReactiveJwtAuthenticationConverterAdapter(
            jwtAuthenticationConverter);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "https://app.example.com"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
```

#### D) Filtre de Propagation JWT

```java
@Component
public class AuthenticationFilter implements GatewayFilter {
    
    private static final Logger logger = 
        LoggerFactory.getLogger(AuthenticationFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Récupérer le JWT depuis SecurityContext
        return exchange.getPrincipal()
            .filter(principal -> principal instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .map(JwtAuthenticationToken::getToken)
            .map(jwt -> {
                // Ajouter le JWT dans les headers pour les microservices
                ServerHttpRequest mutatedRequest = request.mutate()
                    .header("Authorization", "Bearer " + jwt.getTokenValue())
                    .header("X-User-Id", jwt.getClaimAsString("sub"))
                    .header("X-User-Roles", String.join(",", 
                        jwt.getClaimAsStringList("roles")))
                    .build();
                
                return exchange.mutate().request(mutatedRequest).build();
            })
            .defaultIfEmpty(exchange)
            .flatMap(chain::filter);
    }
}
```

#### E) Filtre Factory pour Configuration Route

```java
@Component
public class AuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<Object> {
    
    private final AuthenticationFilter authenticationFilter;
    
    public AuthenticationGatewayFilterFactory(
            AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }
    
    @Override
    public GatewayFilter apply(Object config) {
        return authenticationFilter;
    }
}
```

### 8.4 Microservice Protégé (Resource Server)

#### A) Configuration User Service

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class UserServiceSecurityConfig {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/users/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtSecret));
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter);
        
        return jwtAuthenticationConverter;
    }
}
```

#### B) Contrôleur avec Sécurité

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        // Le JWT est déjà validé par Spring Security
        String username = authentication.getName();
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessUser(#id, authentication)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.canEditUser(#id, authentication)")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
}
```

#### C) Security Expression Personnalisée

```java
@Component("userSecurity")
public class UserSecurityExpression {
    
    public boolean canAccessUser(Long userId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        // Admin peut tout voir
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        
        // L'utilisateur peut voir son propre profil
        String currentUserId = getUserIdFromAuthentication(authentication);
        return userId.toString().equals(currentUserId);
    }
    
    public boolean canEditUser(Long userId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        // Admin peut tout modifier
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        
        // L'utilisateur peut modifier son propre profil
        String currentUserId = getUserIdFromAuthentication(authentication);
        return userId.toString().equals(currentUserId);
    }
    
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
    
    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("sub");
        }
        return null;
    }
}
```

### 8.5 Communication Inter-Services Sécurisée

#### A) Feign Client avec JWT Propagation

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
@FeignClient(
    name = "user-service",
    configuration = FeignClientConfiguration.class
)
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);
    
    @GetMapping("/api/users/username/{username}")
    UserDto getUserByUsername(@PathVariable String username);
}

@Configuration
public class FeignClientConfiguration {
    
    @Bean
    public RequestInterceptor jwtTokenInterceptor() {
        return requestTemplate -> {
            // Récupérer le JWT du contexte de sécurité
            Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
            
            if (authentication != null && 
                    authentication.getCredentials() instanceof Jwt jwt) {
                
                // Ajouter le token aux headers
                requestTemplate.header(
                    "Authorization", 
                    "Bearer " + jwt.getTokenValue()
                );
            }
        };
    }
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}
```

#### B) RestTemplate avec JWT

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Ajouter intercepteur pour JWT
        restTemplate.setInterceptors(
            Collections.singletonList(jwtTokenInterceptor())
        );
        
        return restTemplate;
    }
    
    @Bean
    public ClientHttpRequestInterceptor jwtTokenInterceptor() {
        return (request, body, execution) -> {
            Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
            
            if (authentication != null && 
                    authentication.getCredentials() instanceof String token) {
                
                request.getHeaders().add(
                    "Authorization", 
                    "Bearer " + token
                );
            }
            
            return execution.execute(request, body);
        };
    }
}

// Utilisation
@Service
public class OrderService {
    
    private final RestTemplate restTemplate;
    
    public Order createOrder(CreateOrderRequest request) {
        // Le JWT est automatiquement propagé
        UserDto user = restTemplate.getForObject(
            "http://user-service/api/users/{id}",
            UserDto.class,
            request.getUserId()
        );
        
        // Logique de création de commande
        // ...
    }
}
```

### 8.6 Audit et Logging Sécurisé

```java
@Aspect
@Component
public class SecurityAuditAspect {
    
    private static final Logger logger = 
        LoggerFactory.getLogger(SecurityAuditAspect.class);
    
    private final AuditEventRepository auditEventRepository;
    
    @Around("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public Object auditSecuredMethod(ProceedingJoinPoint joinPoint) 
            throws Throwable {
        
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        String username = authentication != null ? 
            authentication.getName() : "anonymous";
        
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Log succès
            logger.info("AUDIT: User {} successfully executed {} in {}ms",
                username, methodName, duration);
            
            // Sauvegarder en base
            saveAuditEvent(username, methodName, "SUCCESS", duration, null);
            
            return result;
            
        } catch (AccessDeniedException e) {
            // Log accès refusé
            logger.warn("AUDIT: User {} denied access to {}",
                username, methodName);
            
            saveAuditEvent(username, methodName, "ACCESS_DENIED", 0, 
                e.getMessage());
            
            throw e;
            
        } catch (Exception e) {
            // Log erreur
            logger.error("AUDIT: User {} encountered error in {}: {}",
                username, methodName, e.getMessage());
            
            saveAuditEvent(username, methodName, "ERROR", 0, e.getMessage());
            
            throw e;
        }
    }
    
    private void saveAuditEvent(
            String username,
            String action,
            String status,
            long duration,
            String errorMessage) {
        
        AuditEvent event = AuditEvent.builder()
            .username(username)
            .action(action)
            .status(status)
            .duration(duration)
            .errorMessage(errorMessage)
            .ipAddress(getCurrentIpAddress())
            .timestamp(Instant.now())
            .build();
        
        auditEventRepository.save(event);
    }
    
    private String getCurrentIpAddress() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes();
        
        HttpServletRequest request = attributes.getRequest();
        
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        
        return ipAddress;
    }
}
```

### 8.7 Configuration Complète application.yml

```yaml
# Service Configuration
spring:
  application:
    name: user-service
  
  # Database
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  # Flyway Migration
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  # Redis (for caching and rate limiting)
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}
  
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes

# JWT Configuration
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000        # 24h
    refresh-expiration: 604800000  # 7 days

# Eureka Client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    registry-fetch-interval-seconds: 5
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

# Logging
logging:
  level:
    com.example: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/user-service.log
    max-size: 10MB
    max-history: 30

# Resilience4j Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      userService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

---

## 9. Exemples Pratiques Complets

### 9.1 Application E-Commerce Complète

**Architecture :**
```
- api-gateway (Port 8080)
- auth-service (Port 8081)
- user-service (Port 8082)
- product-service (Port 8083)
- order-service (Port 8084)
- payment-service (Port 8085)
- eureka-server (Port 8761)
```

#### Flux d'une Commande Sécurisée

```java
// 1. Client s'authentifie
POST http://localhost:8080/api/auth/login
{
  "username": "john.doe",
  "password": "SecurePass123!"
}

Response: 
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}

// 2. Client crée une commande
POST http://localhost:8080/api/orders
Authorization: Bearer eyJhbGc...
{
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 5, "quantity": 1}
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "Paris",
    "zipCode": "75001"
  }
}

// 3. Order Service vérifie les produits et calcule le total
@Service
public class OrderService {
    
    private final ProductServiceClient productClient;
    private final UserServiceClient userClient;
    private final PaymentServiceClient paymentClient;
    
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        // Récupérer l'utilisateur courant
        Authentication auth = SecurityContextHolder.getContext()
            .getAuthentication();
        String username = auth.getName();
        
        UserDto user = userClient.getUserByUsername(username);
        
        // Vérifier les produits et calculer le total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (var item : request.getItems()) {
            ProductDto product = productClient.getProduct(item.getProductId());
            
            // Vérifier stock
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                    "Not enough stock for product: " + product.getName());
            }
            
            OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(item.getQuantity())
                .price(product.getPrice())
                .build();
            
            orderItems.add(orderItem);
            total = total.add(
                product.getPrice().multiply(
                    BigDecimal.valueOf(item.getQuantity())
                )
            );
        }
        
        // Créer la commande
        Order order = Order.builder()
            .userId(user.getId())
            .items(orderItems)
            .total(total)
            .status(OrderStatus.PENDING)
            .shippingAddress(request.getShippingAddress())
            .createdAt(Instant.now())
            .build();
        
        order = orderRepository.save(order);
        
        // Initier le paiement
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderId(order.getId())
            .amount(total)
            .currency("EUR")
            .build();
        
        PaymentDto payment = paymentClient.processPayment(paymentRequest);
        
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            order.setStatus(OrderStatus.CONFIRMED);
            
            // Déduire du stock
            for (var item : orderItems) {
                productClient.decreaseStock(
                    item.getProductId(), 
                    item.getQuantity()
                );
            }
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }
        
        order = orderRepository.save(order);
        
        return OrderDto.fromOrder(order);
    }
}
```

### 9.2 Testing de Sécurité

```java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    private String adminToken;
    private String userToken;
    
    @BeforeEach
    void setup() {
        // Créer utilisateurs de test
        User admin = createUser("admin", "ROLE_ADMIN");
        User user = createUser("user", "ROLE_USER");
        
        // Générer tokens
        adminToken = generateToken(admin);
        userToken = generateToken(user);
    }
    
    @Test
    @DisplayName("Accès endpoint admin avec token user - devrait échouer")
    void testAdminEndpointWithUserToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Accès endpoint admin avec token admin - devrait réussir")
    void testAdminEndpointWithAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Accès sans token - devrait échouer")
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Accès avec token expiré - devrait échouer")
    void testWithExpiredToken() throws Exception {
        String expiredToken = generateExpiredToken();
        
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Login avec credentials valides")
    void testLoginWithValidCredentials() throws Exception {
        String loginJson = """
            {
                "username": "user",
                "password": "password"
            }
            """;
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists());
    }
    
    @Test
    @DisplayName("Login avec mauvais password")
    void testLoginWithInvalidPassword() throws Exception {
        String loginJson = """
            {
                "username": "user",
                "password": "wrongpassword"
            }
            """;
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Modifier son propre profil - devrait réussir")
    void testUpdateOwnProfile() throws Exception {
        String updateJson = """
            {
                "firstName": "Updated",
                "lastName": "Name"
            }
            """;
        
        mockMvc.perform(put("/api/users/1")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Modifier profil d'un autre user - devrait échouer")
    void testUpdateOtherUserProfile() throws Exception {
        String updateJson = """
            {
                "firstName": "Hacker",
                "lastName": "Attack"
            }
            """;
        
        mockMvc.perform(put("/api/users/999")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isForbidden());
    }
    
    private User createUser(String username, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail(username + "@test.com");
        user.setEnabled(true);
        
        Role userRole = roleRepository.findByName(role)
            .orElseGet(() -> {
                Role r = new Role();
                r.setName(role);
                return roleRepository.save(r);
            });
        
        user.setRoles(Set.of(userRole));
        
        return userRepository.save(user);
    }
    
    private String generateToken(User user) {
        UserDetails userDetails = CustomUserDetails.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        return tokenProvider.generateAccessToken(authentication);
    }
    
    private String generateExpiredToken() {
        // Créer un token déjà expiré
        Instant now = Instant.now();
        Instant expiry = now.minus(1, ChronoUnit.HOURS);
        
        return Jwts.builder()
            .setSubject("user")
            .setIssuedAt(Date.from(now.minus(2, ChronoUnit.HOURS)))
            .setExpiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }
}
```

---

## 10. Bonnes Pratiques et Recommandations

### 10.1 Sécurité des Secrets

```java
// ❌ MAUVAIS - Secret en dur
@Value("${jwt.secret:myHardcodedSecret123}")
private String jwtSecret;

// ✅ BON - Variable d'environnement
@Value("${JWT_SECRET}")
private String jwtSecret;

// ✅ MEILLEUR - Vault (HashiCorp, AWS Secrets Manager)
@Configuration
public class VaultConfig {
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.create("vault.example.com", 8200);
        endpoint.setScheme("https");
        
        VaultToken token = VaultToken.of(System.getenv("VAULT_TOKEN"));
        
        return new VaultTemplate(endpoint, 
            new TokenAuthentication(token));
    }
    
    @Bean
    public String jwtSecret(VaultTemplate vaultTemplate) {
        VaultResponse response = vaultTemplate
            .read("secret/data/jwt");
        
        return (String) response.getData().get("secret");
    }
}
```

### 10.2 Rate Limiting

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final RateLimiter rateLimiter;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) 
            throws ServletException, IOException {
        
        String clientId = getClientIdentifier(request);
        
        if (!rateLimiter.tryAcquire(clientId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // Par IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        
        // Ou par utilisateur si authentifié
        Authentication auth = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        return auth != null && auth.isAuthenticated() ?
            auth.getName() : ip;
    }
}
```

### 10.3 Protection CSRF pour SPA

```java
@Configuration
public class CsrfConfig {
    
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = 
            CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }
}

// Frontend (Angular/React)
// Le token sera automatiquement envoyé dans le header X-XSRF-TOKEN
```

---

## Conclusion

Ce guide couvre en profondeur Spring Security 7 :

✅ **Architecture** : Filtres, Authentication, Authorization
✅ **Authentification** : UserDetailsService, AuthenticationManager, Providers
✅ **Autorisation** : Roles, Permissions, Method Security
✅ **Filter Chain** : Ordre d'exécution, filtres personnalisés
✅ **Password Encoding** : BCrypt, Argon2, best practices
✅ **JWT** : Génération, validation, refresh tokens
✅ **OAuth2** : Client et Resource Server
✅ **Microservices** : Gateway, propagation JWT, inter-service communication
✅ **Testing** : Tests d'intégration de sécurité
✅ **Production** : Rate limiting, audit, monitoring

**Points clés à retenir :**
1. Spring Security fonctionne via une chaîne de filtres
2. Séparez toujours Authentication et Authorization
3. Utilisez des algorithmes de hashing forts (Argon2, BCrypt)
4. Tokens JWT = stateless, perfect pour microservices
5. Toujours valider ET vérifier les tokens
6. Utilisez Method Security pour un contrôle fin
7. Auditez tous les accès sécurisés

Pour aller plus loin :
- Keycloak pour un Authorization Server complet
- OAuth2 avec PKCE pour applications mobiles
- mTLS pour communication inter-services
- WebAuthn pour authentification sans mot de passe