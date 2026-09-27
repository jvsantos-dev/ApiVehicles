package com.ford.vehicleapi.service;

import com.ford.vehicleapi.domain.Role;
import com.ford.vehicleapi.domain.User;
import com.ford.vehicleapi.dto.auth.LoginRequest;
import com.ford.vehicleapi.dto.auth.RegisterRequest;
import com.ford.vehicleapi.dto.auth.TokenResponse;
import com.ford.vehicleapi.dto.user.UserResponse;
import com.ford.vehicleapi.exception.ConflictException;
import com.ford.vehicleapi.repository.UserRepository;
import com.ford.vehicleapi.security.TokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                // Mesma mensagem para e-mail inexistente e senha errada: não revela quais e-mails existem.
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        TokenService.IssuedToken token = tokenService.issue(user);
        return new TokenResponse(token.value(), "Bearer", token.expiresInSeconds(), token.expiresAt(),
                UserResponse.from(user));
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Já existe um usuário cadastrado com o e-mail " + email);
        }
        User user = new User(request.name().trim(), email, passwordEncoder.encode(request.password()), Role.CUSTOMER);
        return UserResponse.from(userRepository.save(user));
    }
}
