package com.example.M.E.N.O.S.controller;

import com.example.M.E.N.O.S.dto.CurtidaRequest;
import com.example.M.E.N.O.S.dto.CurtidasMenu;
import com.example.M.E.N.O.S.model.Curtida;
import com.example.M.E.N.O.S.service.CurtidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.M.E.N.O.S.seguranca.Acesso;
import com.example.M.E.N.O.S.seguranca.AutenticacaoFilter;
import com.example.M.E.N.O.S.seguranca.Sessao;

@RestController
@RequestMapping("/curtidas")
public class CurtidaController {

    private final CurtidaService curtidaService;

    public CurtidaController(CurtidaService curtidaService) {
        this.curtidaService = curtidaService;
    }

    // Aluno curte um cardápio
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Curtida curtir(@Valid @RequestBody CurtidaRequest request,
                           @RequestAttribute(AutenticacaoFilter.SESSAO) Sessao sessao) {
        Acesso.exigirAluno(sessao, request.estudanteId());
        return curtidaService.curtir(request);
    }

    // Aluno tira a curtida: DELETE /curtidas?estudanteId=1&menuId=2
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void descurtir(@RequestParam Long estudanteId, @RequestParam Long menuId) {
        curtidaService.descurtir(estudanteId, menuId);
    }

    // Total de curtidas do cardápio. Com ?estudanteId=1 diz também se esse aluno já curtiu.
    @GetMapping("/menu/{menuId}")
    public CurtidasMenu contar(@PathVariable Long menuId,
                               @RequestParam(required = false) Long estudanteId) {
        return curtidaService.contar(menuId, estudanteId);
    }
}
