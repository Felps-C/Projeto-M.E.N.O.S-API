package com.example.M.E.N.O.S.seguranca;

import java.util.Arrays;
import java.util.List;

// Lê a lista "menos.cors.origens" (sites que podem chamar a API) para o filtro de segurança.
// O Spring já aplica essa lista nas respostas normais; o filtro precisa dela porque suas recusas
// (401/403) acontecem antes do Spring, e sem os cabeçalhos de CORS o navegador as mostra como "erro de rede".
public final class OrigensPermitidas {

    private final List<String> origens;

    public OrigensPermitidas(String[] origens) {
        this.origens = Arrays.stream(origens).map(String::trim).filter(o -> !o.isEmpty()).toList();
    }

    // O valor para "Access-Control-Allow-Origin", ou null quando este site não está liberado.
    public String paraResposta(String origemDaRequisicao) {
        if (origemDaRequisicao == null) return null;
        if (origens.contains("*")) return origemDaRequisicao;
        return origens.contains(origemDaRequisicao) ? origemDaRequisicao : null;
    }
}
