package com.example.M.E.N.O.S.controller;

import com.example.M.E.N.O.S.dto.LoginRequest;
import com.example.M.E.N.O.S.dto.LoginResponse;
import com.example.M.E.N.O.S.seguranca.Sessao;
import com.example.M.E.N.O.S.seguranca.TokenJwt;
import com.example.M.E.N.O.S.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final TokenJwt tokens;

    public AuthController(AuthService authService, TokenJwt tokens) {
        this.authService = authService;
        this.tokens = tokens;
    }

    // Login de escola (CNPJ ou e-mail) e de aluno (matrícula ou e-mail).
    // Devolve { tipo, id, escolaId, nome, token }. Os demais endpoints exigem esse token.
    @PostMapping("/login")
    public LoginResponse entrar(@Valid @RequestBody LoginRequest request) {
        LoginResponse resposta = authService.entrar(request);
        return resposta.comToken(tokens.gerar(new Sessao(resposta.id(), resposta.tipo(), resposta.escolaId())));
    }
}
