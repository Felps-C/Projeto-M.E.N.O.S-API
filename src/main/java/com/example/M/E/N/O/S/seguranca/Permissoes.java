package com.example.M.E.N.O.S.seguranca;

import java.util.function.Function;

// Quem pode chamar o quê. Fica tudo aqui, num lugar só, para ser fácil de ler e de testar.
// Regras: a escola só mexe nos dados da PRÓPRIA escola; o aluno só lê o que é da sua escola
// e só age em nome de si mesmo (votar, curtir, comentar, editar o próprio perfil).
// Limite conhecido: rotas por id solto (ex.: /produtos/5, /cardapios/3) ainda não conferem a qual
// escola o item pertence; isso exige consultar o banco e fica para a próxima etapa.
public final class Permissoes {

    static final String FAZER_LOGIN = "Faça login para continuar";
    static final String SO_ESCOLA = "Esta ação é permitida apenas para a escola";
    static final String SO_ALUNO = "Esta ação é permitida apenas para alunos";
    static final String OUTRA_ESCOLA = "Você não tem acesso aos dados de outra escola";
    static final String OUTRO_ALUNO = "Você só pode acessar os seus próprios dados";
    static final String INFORME_ESCOLA = "Informe a escola na consulta";
    static final String NEGADO = "Acesso não permitido";

    private Permissoes() {
    }

    // Rotas abertas, sem login: entrar, cadastrar escola/aluno, listar escolas (tela de cadastro) e fotos dos pratos.
    public static boolean publico(String metodo, String caminho) {
        String[] p = partes(caminho);
        String m = metodo.toUpperCase();
        if (p.length == 1 && p[0].equals("login")) return m.equals("POST");
        if (p.length == 1 && p[0].equals("escolas")) return m.equals("POST") || m.equals("GET");
        if (p.length == 1 && p[0].equals("estudantes")) return m.equals("POST");
        if (p.length == 1 && p[0].equals("error")) return true;
        // As fotos são carregadas por <img>, que não consegue enviar o token.
        return p.length == 3 && p[0].equals("merendas") && p[2].equals("imagem") && m.equals("GET");
    }

    // null = permitido; senão, o motivo da recusa (HTTP 403).
    public static String verificar(String metodo, String caminho, Sessao s, Function<String, String> parametro) {
        String[] p = partes(caminho);
        String m = metodo.toUpperCase();
        boolean leitura = m.equals("GET");
        if (p.length == 0) return NEGADO;

        switch (p[0]) {
            case "escolas":
                // /escolas/{id}: qualquer um lê a própria escola; só a escola altera/apaga a si mesma.
                if (p.length == 2) {
                    if (!mesmaEscola(p[1], s)) return OUTRA_ESCOLA;
                    return (leitura || s.ehEscola()) ? null : SO_ESCOLA;
                }
                return NEGADO;

            case "estudantes":
                if (p.length == 1) { // lista de alunos: só a escola, e só da própria escola
                    if (!leitura) return NEGADO;
                    if (!s.ehEscola()) return SO_ESCOLA;
                    return escolaDoParametro(parametro.apply("escolaId"), s);
                }
                if (p.length == 2) {
                    boolean proprio = s.ehOAluno(numero(p[1]));
                    if (m.equals("PUT")) return proprio ? null : OUTRO_ALUNO;
                    if (leitura || m.equals("DELETE")) return (s.ehEscola() || proprio) ? null : OUTRO_ALUNO;
                }
                return NEGADO;

            case "enquetes":
                return enquetes(p, m, s);

            case "cardapios":
                return cardapios(p, m, s);

            case "merendas":
                if (p.length == 1) return NEGADO; // lista de todas as escolas: use /merendas/escola/{id}
                if (p.length == 3 && p[1].equals("escola")) {
                    if (!mesmaEscola(p[2], s)) return OUTRA_ESCOLA;
                    return (leitura || s.ehEscola()) ? null : SO_ESCOLA;
                }
                return (leitura || s.ehEscola()) ? null : SO_ESCOLA;

            case "estoques":
            case "produtos":
            case "listas-compra":
            case "relatorios":
                if (!s.ehEscola()) return SO_ESCOLA;
                if (p.length >= 3 && p[1].equals("escola") && !mesmaEscola(p[2], s)) return OUTRA_ESCOLA;
                return null;

            case "feedbacks":
                if (p.length == 1 && leitura) {
                    String escola = parametro.apply("escolaId");
                    if (escola != null) return escolaDoParametro(escola, s);
                    return parametro.apply("menuId") != null ? null : INFORME_ESCOLA;
                }
                if (p.length == 1 && m.equals("POST")) return s.ehAluno() ? null : SO_ALUNO; // autor conferido no controller
                if (p.length == 2 && leitura) return null;
                if (p.length == 2 && m.equals("PUT")) return s.ehAluno() ? null : SO_ALUNO;   // autor conferido no controller
                if (p.length == 2 && m.equals("DELETE")) return null;                          // autor/escola: controller
                return NEGADO;

            case "curtidas":
                if (!s.ehAluno()) {
                    return (p.length == 3 && leitura) ? null : SO_ALUNO;
                }
                // O aluno só consulta/remove curtida em nome dele (o POST é conferido no controller).
                String estudante = parametro.apply("estudanteId");
                if (estudante != null && !s.ehOAluno(numero(estudante))) return OUTRO_ALUNO;
                if (p.length == 1 && m.equals("POST")) return null;
                if (p.length == 1 && m.equals("DELETE")) return estudante != null ? null : OUTRO_ALUNO;
                if (p.length == 3 && p[1].equals("menu") && leitura) return null;
                return NEGADO;

            default:
                return NEGADO;
        }
    }

    private static String enquetes(String[] p, String m, Sessao s) {
        boolean leitura = m.equals("GET");
        if (p.length == 3 && p[1].equals("escola")) { // /enquetes/escola/{id}
            if (!mesmaEscola(p[2], s)) return OUTRA_ESCOLA;
            return (leitura || s.ehEscola()) ? null : SO_ESCOLA;
        }
        if (p.length == 2) { // /enquetes/{id}
            return (leitura || s.ehEscola()) ? null : SO_ESCOLA;
        }
        if (p.length == 3 && p[2].equals("resultado") && leitura) return null;
        if (p.length == 3 && p[2].equals("votos") && m.equals("POST")) return s.ehAluno() ? null : SO_ALUNO; // aluno conferido no controller
        // opções, abrir e fechar
        return s.ehEscola() ? null : SO_ESCOLA;
    }

    private static String cardapios(String[] p, String m, Sessao s) {
        boolean leitura = m.equals("GET");
        if (p.length == 3 && p[1].equals("escola")) { // /cardapios/escola/{id}
            if (!mesmaEscola(p[2], s)) return OUTRA_ESCOLA;
            return (leitura || s.ehEscola()) ? null : SO_ESCOLA;
        }
        if (leitura) return null; // /cardapios/{id} e /cardapios/{id}/resumo
        return s.ehEscola() ? null : SO_ESCOLA;
    }

    private static String escolaDoParametro(String escolaId, Sessao s) {
        if (escolaId == null) return INFORME_ESCOLA;
        return mesmaEscola(escolaId, s) ? null : OUTRA_ESCOLA;
    }

    private static boolean mesmaEscola(String id, Sessao s) {
        Long numero = numero(id);
        return numero != null && numero.equals(s.escolaId());
    }

    private static Long numero(String texto) {
        try {
            return Long.valueOf(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    // "/cardapios//escola/1/" -> ["cardapios", "escola", "1"]
    static String[] partes(String caminho) {
        String limpo = caminho == null ? "" : caminho.trim();
        int ponto = limpo.indexOf(';'); // ;jsessionid=...
        if (ponto >= 0) limpo = limpo.substring(0, ponto);
        StringBuilder sb = new StringBuilder();
        for (String parte : limpo.split("/")) {
            if (!parte.isEmpty()) sb.append(sb.length() == 0 ? "" : "/").append(parte);
        }
        return sb.length() == 0 ? new String[0] : sb.toString().split("/");
    }
}
