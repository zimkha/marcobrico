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
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // l'un
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // ou l'autre
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtService;
    private final UserService userService;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
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
            throw new RuntimeException("Username already exist");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exist");
        }

        // Checker if the password is weak
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(request.password());
        if (decision.isCompromised()) throw new IllegalArgumentException("Password is compromised, please change your password");

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userRepository.save(user);
    }

}
