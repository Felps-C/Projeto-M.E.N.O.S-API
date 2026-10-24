package com.example.M.E.N.O.S.controller;

import com.example.M.E.N.O.S.dto.EnqueteRequest;
import com.example.M.E.N.O.S.dto.EnqueteUpdateRequest;
import com.example.M.E.N.O.S.dto.OpcaoRequest;
import com.example.M.E.N.O.S.dto.ResultadoEnquete;
import com.example.M.E.N.O.S.dto.VotoRequest;
import com.example.M.E.N.O.S.model.Enquete;
import com.example.M.E.N.O.S.model.Voto;
import com.example.M.E.N.O.S.service.EnqueteService;
import com.example.M.E.N.O.S.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.M.E.N.O.S.seguranca.Acesso;
import com.example.M.E.N.O.S.seguranca.AutenticacaoFilter;
import com.example.M.E.N.O.S.seguranca.Sessao;

import java.util.List;

@RestController
@RequestMapping("/enquetes")
public class EnqueteController {

    private final EnqueteService enqueteService;
    private final VotoService votoService;

    public EnqueteController(EnqueteService enqueteService, VotoService votoService) {
        this.enqueteService = enqueteService;
        this.votoService = votoService;
    }

    // Escola cria uma enquete (já com as opções)
    @PostMapping("/escola/{escolaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Enquete criar(@PathVariable Long escolaId, @Valid @RequestBody EnqueteRequest request) {
        return enqueteService.criar(escolaId, request);
    }

    // Use ?ativa=true para ver só as enquetes ativas.
    @GetMapping("/escola/{escolaId}")
    public List<Enquete> listarPorEscola(@PathVariable Long escolaId,
                                         @RequestParam(required = false) Boolean ativa) {
        return enqueteService.listarPorEscola(escolaId, ativa);
    }

    @GetMapping("/{id}")
    public Enquete buscarPorId(@PathVariable Long id) {
        return enqueteService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Enquete atualizar(@PathVariable Long id, @Valid @RequestBody EnqueteUpdateRequest request) {
        return enqueteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        enqueteService.deletar(id);
    }

    @PostMapping("/{id}/opcoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Enquete adicionarOpcao(@PathVariable Long id, @Valid @RequestBody OpcaoRequest request) {
        return enqueteService.adicionarOpcao(id, request);
    }

    @DeleteMapping("/{id}/opcoes/{opcaoId}")
    public Enquete removerOpcao(@PathVariable Long id, @PathVariable Long opcaoId) {
        return enqueteService.removerOpcao(id, opcaoId);
    }

    @PostMapping("/{id}/abrir")
    public Enquete abrir(@PathVariable Long id) {
        return enqueteService.abrir(id);
    }

    @PostMapping("/{id}/fechar")
    public Enquete fechar(@PathVariable Long id) {
        return enqueteService.fechar(id);
    }

    // Aluno vota
    @PostMapping("/{id}/votos")
    @ResponseStatus(HttpStatus.CREATED)
    public Voto votar(@PathVariable Long id, @Valid @RequestBody VotoRequest request,
                       @RequestAttribute(AutenticacaoFilter.SESSAO) Sessao sessao) {
        Acesso.exigirAluno(sessao, request.estudanteId());
        return votoService.votar(id, request);
    }

    @GetMapping("/{id}/resultado")
    public ResultadoEnquete resultado(@PathVariable Long id) {
        return enqueteService.resultado(id);
    }
}
