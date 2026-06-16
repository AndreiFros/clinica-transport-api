package com.andrei.clinica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// ─── O que é CORS? ────────────────────────────────────────────────────────────
// CORS = Cross-Origin Resource Sharing
//
// O navegador bloqueia requisições entre origens diferentes por segurança.
// "Origem" = protocolo + domínio + porta.
//
// Seu frontend:  file:///C:/Users/.../login.html  (origem: file://)
// Sua API:       http://localhost:8080             (origem: http://localhost:8080)
//
// Como são origens diferentes, o navegador bloqueia com "Failed to fetch".
// Essa configuração diz à API: "pode aceitar requisições dessas origens".
// ─────────────────────────────────────────────────────────────────────────────
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Origens permitidas:
        // - file:// → frontend aberto direto como arquivo no navegador
        // - http://localhost:* → frontend rodando em qualquer porta local
        // - null → alguns navegadores mandam "null" quando abre arquivo local
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedOrigin("null"); // para arquivo local (file://)

        // Métodos HTTP permitidos
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Headers permitidos
        // Authorization é o header do JWT — sem isso o token é bloqueado
        config.addAllowedHeader("*");

        // Permite que o frontend leia os headers da resposta
        config.setAllowCredentials(false);

        // Aplica essa configuração em TODOS os endpoints da API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
