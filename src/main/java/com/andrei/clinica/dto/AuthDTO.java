package com.andrei.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Nome é obrigatório")
        private String nome;

        private String empresa;

        @Email(message = "E-mail inválido")
        @NotBlank(message = "E-mail é obrigatório")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String nome;
        private String email;
        private String empresa;

        public AuthResponse(String token, String nome, String email, String empresa) {
            this.token = token;
            this.nome = nome;
            this.email = email;
            this.empresa = empresa;
        }
    }
}
