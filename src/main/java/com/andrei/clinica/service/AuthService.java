package com.andrei.clinica.service;

import com.andrei.clinica.dto.AuthDTO;
import com.andrei.clinica.exception.BusinessException;
import com.andrei.clinica.model.User;
import com.andrei.clinica.repository.UserRepository;
import com.andrei.clinica.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado");
        }
        User user = User.builder()
                .nome(request.getNome())
                .empresa(request.getEmpresa())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthDTO.AuthResponse(token, user.getNome(), user.getEmail(), user.getEmpresa());
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        String token = jwtService.generateToken(user);
        return new AuthDTO.AuthResponse(token, user.getNome(), user.getEmail(), user.getEmpresa());
    }
}
