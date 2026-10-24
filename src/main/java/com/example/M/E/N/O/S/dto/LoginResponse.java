package com.example.M.E.N.O.S.dto;

// Resposta do login. É o que o front-end guarda: a sessão ({ tipo, id, escolaId, nome }) e o token,
// que o front envia em todas as chamadas seguintes (cabeçalho "Authorization: Bearer <token>").
// tipo = "escola" ou "aluno". Para a escola, id e escolaId são iguais.
public record LoginResponse(String tipo, Long id, Long escolaId, String nome, String token) {

    // Usado pelo AuthService, que não conhece o token.
    public LoginResponse(String tipo, Long id, Long escolaId, String nome) {
        this(tipo, id, escolaId, nome, null);
    }

    public LoginResponse comToken(String novoToken) {
        return new LoginResponse(tipo, id, escolaId, nome, novoToken);
    }
}
