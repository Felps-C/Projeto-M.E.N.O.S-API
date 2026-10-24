package com.example.M.E.N.O.S.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

// Porteiro da API: toda requisição passa por aqui antes de chegar nos controllers.
// Sem token válido -> 401. Com token, mas sem permissão para aquela rota -> 403.
@Component
public class AutenticacaoFilter extends OncePerRequestFilter {

    // Nome com que a sessão fica disponível nos controllers: @RequestAttribute(AutenticacaoFilter.SESSAO).
    public static final String SESSAO = "menos.sessao";

    private final TokenJwt tokens;
    private final OrigensPermitidas origens;

    public AutenticacaoFilter(TokenJwt tokens,
                              @Value("${menos.cors.origens:http://localhost:5173}") String[] origens) {
        this.tokens = tokens;
        this.origens = new OrigensPermitidas(origens);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String metodo = requisicao.getMethod();
        String caminho = requisicao.getRequestURI();

        // OPTIONS é a "pré-checagem" do navegador (CORS): não leva token e não tem dados.
        if ("OPTIONS".equalsIgnoreCase(metodo) || Permissoes.publico(metodo, caminho)) {
            cadeia.doFilter(requisicao, resposta);
            return;
        }

        String cabecalho = requisicao.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            recusar(requisicao, resposta, HttpServletResponse.SC_UNAUTHORIZED, Permissoes.FAZER_LOGIN);
            return;
        }
        Optional<Sessao> sessao = tokens.validar(cabecalho.substring("Bearer ".length()).trim());
        if (sessao.isEmpty()) {
            recusar(requisicao, resposta, HttpServletResponse.SC_UNAUTHORIZED, "Sessão expirada. Entre novamente");
            return;
        }

        String motivo = Permissoes.verificar(metodo, caminho, sessao.get(), requisicao::getParameter);
        if (motivo != null) {
            recusar(requisicao, resposta, HttpServletResponse.SC_FORBIDDEN, motivo);
            return;
        }

        requisicao.setAttribute(SESSAO, sessao.get());
        cadeia.doFilter(requisicao, resposta);
    }

    // Mesmo formato dos outros erros da API ({ "message": "..." }), que o front já sabe mostrar.
    // Leva os cabeçalhos de CORS para o site liberado conseguir ler o erro (ex.: voltar ao login quando a sessão vence).
    private void recusar(HttpServletRequest requisicao, HttpServletResponse resposta, int status, String mensagem)
            throws IOException {
        String origem = origens.paraResposta(requisicao.getHeader("Origin"));
        if (origem != null) {
            resposta.setHeader("Access-Control-Allow-Origin", origem);
            resposta.setHeader("Vary", "Origin");
        }
        resposta.setStatus(status);
        resposta.setContentType("application/json;charset=UTF-8");
        resposta.getOutputStream().write(("{\"message\":\"" + mensagem + "\"}").getBytes(StandardCharsets.UTF_8));
    }
}
