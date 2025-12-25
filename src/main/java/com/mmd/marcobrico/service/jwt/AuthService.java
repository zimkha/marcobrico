package com.mmd.marcobrico.service.jwt;

import com.mmd.marcobrico.domain.Role;
import com.mmd.marcobrico.domain.User;
import com.mmd.marcobrico.dto.LoginRequest;
import com.mmd.marcobrico.dto.user.AuthResponse;
import com.mmd.marcobrico.dto.user.RegisterRequest;
import com.mmd.marcobrico.repository.UserRepository;
import com.mmd.marcobrico.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtService;
    private final UserService userService;
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );


        UserDetails userDetails = userService.loadUserByUsername(request.username());

        String token = jwtService.generateJwtToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow();

        return new AuthResponse(
                token,
                userDetails.getUsername(),
                role
        );
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username déjà utilisé");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userRepository.save(user);
    }

}
