package com.andrei.clinica.controller;

import com.andrei.clinica.dto.AuthDTO;
import com.andrei.clinica.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ════════════════════════════════════════════════════════════════
// AuthController — porta de entrada para cadastro e login
//
// RESPONSABILIDADE ÚNICA:
// Esse controller só lida com autenticação. Não valida dados de
// negócio, não acessa banco diretamente, não sabe nada sobre
// operações clínicas. Só recebe o request e passa pro AuthService.
//
// POR QUE ISSO É EFICIENTE:
// Se amanhã você quiser mudar a regra de senha (ex: exigir símbolo),
// você só mexe no AuthService. O controller não precisa saber disso.
// Cada classe tem uma função — isso se chama Princípio da
// Responsabilidade Única (SRP) do SOLID.
// ════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // @RequiredArgsConstructor + final = injeção de dependência automática.
    // O Spring cria o AuthService e injeta aqui sem você precisar escrever
    // o construtor manualmente. Funciona porque AuthService tem @Service.
    private final AuthService authService;

    // ── POST /api/auth/register ───────────────────────────────────────────────
    //
    // Recebe: { "nome": "...", "empresa": "...", "email": "...", "password": "..." }
    // Retorna: { "token": "eyJ...", "nome": "...", "email": "...", "empresa": "..." }
    // Status: 201 Created (por convenção REST: criação = 201, não 200)
    //
    // @Valid → antes de entrar no método, o Spring verifica se os campos
    // passam nas anotações do RegisterRequest (@NotBlank, @Email, @Size).
    // Se falhar, lança MethodArgumentNotValidException que o
    // GlobalExceptionHandler captura e retorna 400 automaticamente.
    // Você não precisa escrever if (nome == null) em lugar nenhum.
    @PostMapping("/register")
    public ResponseEntity<AuthDTO.AuthResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────
    //
    // Recebe: { "email": "...", "password": "..." }
    // Retorna: { "token": "eyJ...", ... }
    // Status: 200 OK
    //
    // Por que 200 e não 201? Porque o login não CRIA nada — só autentica.
    // O ResponseEntity.ok() é um atalho para status 200.
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.AuthResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
