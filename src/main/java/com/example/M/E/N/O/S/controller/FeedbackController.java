package com.example.M.E.N.O.S.controller;

import com.example.M.E.N.O.S.dto.FeedbackRequest;
import com.example.M.E.N.O.S.model.Feedback;
import com.example.M.E.N.O.S.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.M.E.N.O.S.seguranca.Acesso;
import com.example.M.E.N.O.S.seguranca.AutenticacaoFilter;
import com.example.M.E.N.O.S.seguranca.Sessao;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // ?menuId=1 -> feedbacks de um cardápio | ?escolaId=1 -> feedbacks de uma escola
    @GetMapping
    public List<Feedback> listar(@RequestParam(required = false) Long menuId,
                                 @RequestParam(required = false) Long escolaId) {
        return feedbackService.listar(menuId, escolaId);
    }

    @GetMapping("/{id}")
    public Feedback buscarPorId(@PathVariable Long id) {
        return feedbackService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Feedback cadastrar(@Valid @RequestBody FeedbackRequest request,
                               @RequestAttribute(AutenticacaoFilter.SESSAO) Sessao sessao) {
        Acesso.exigirAluno(sessao, request.estudanteId());
        return feedbackService.criar(request);
    }

    @PutMapping("/{id}")
    public Feedback editar(@PathVariable Long id, @Valid @RequestBody FeedbackRequest request,
                            @RequestAttribute(AutenticacaoFilter.SESSAO) Sessao sessao) {
        // Só o autor edita o próprio comentário.
        Acesso.exigirAluno(sessao, feedbackService.buscarPorId(id).getEstudanteId());
        return feedbackService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @RequestAttribute(AutenticacaoFilter.SESSAO) Sessao sessao) {
        // O autor apaga o seu comentário; a escola pode apagar qualquer um (moderação).
        if (sessao.ehAluno()) {
            Acesso.exigirAluno(sessao, feedbackService.buscarPorId(id).getEstudanteId());
        }
        feedbackService.deletar(id);
    }
}
