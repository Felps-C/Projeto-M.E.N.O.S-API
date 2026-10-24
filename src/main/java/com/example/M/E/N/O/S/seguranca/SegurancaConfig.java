package com.example.M.E.N.O.S.seguranca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Configuration
public class SegurancaConfig {

    private static final Logger log = LoggerFactory.getLogger(SegurancaConfig.class);

    // menos.jwt.segredo: defina a variável de ambiente MENOS_JWT_SEGREDO (mínimo 32 caracteres) em produção.
    // Sem ela, é gerado um segredo aleatório a cada início: serve para desenvolver, mas todo mundo
    // precisa entrar de novo quando o servidor reinicia.
    @Bean
    public TokenJwt tokenJwt(@Value("${menos.jwt.segredo:}") String segredo,
                             @Value("${menos.jwt.horas:12}") long horas) {
        byte[] chave;
        if (segredo == null || segredo.isBlank()) {
            chave = new byte[48];
            new SecureRandom().nextBytes(chave);
            log.warn("MENOS_JWT_SEGREDO não definido: usando um segredo temporário (os logins caem ao reiniciar).");
        } else if (segredo.length() < 32) {
            throw new IllegalStateException("menos.jwt.segredo deve ter pelo menos 32 caracteres");
        } else {
            chave = segredo.getBytes(StandardCharsets.UTF_8);
        }
        return new TokenJwt(chave, horas * 3600);
    }
}
