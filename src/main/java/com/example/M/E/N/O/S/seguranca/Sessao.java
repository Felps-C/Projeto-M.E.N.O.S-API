package com.example.M.E.N.O.S.seguranca;

// Quem está fazendo a requisição, lido do token (nunca do corpo ou da URL, que o cliente pode inventar).
// tipo = "escola" ou "aluno" (os mesmos valores do login). Para a escola, id e escolaId são iguais.
public record Sessao(Long id, String tipo, Long escolaId) {

    public static final String ESCOLA = "escola";
    public static final String ALUNO = "aluno";

    public boolean ehEscola() {
        return ESCOLA.equals(tipo);
    }

    public boolean ehAluno() {
        return ALUNO.equals(tipo);
    }

    // true se esta sessão é o aluno com esse id.
    public boolean ehOAluno(Long estudanteId) {
        return ehAluno() && id != null && id.equals(estudanteId);
    }
}
