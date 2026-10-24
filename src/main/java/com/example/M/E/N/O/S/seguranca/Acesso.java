package com.example.M.E.N.O.S.seguranca;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Conferências que dependem do conteúdo da requisição (o filtro só enxerga a URL).
public final class Acesso {

    private Acesso() {
    }

    // Garante que o aluno logado é mesmo o "estudanteId" informado no corpo/consulta.
    public static void exigirAluno(Sessao sessao, Long estudanteId) {
        if (!sessao.ehOAluno(estudanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode agir em nome de si mesmo");
        }
    }
}
