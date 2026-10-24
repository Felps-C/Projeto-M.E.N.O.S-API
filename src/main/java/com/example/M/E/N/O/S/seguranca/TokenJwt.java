package com.example.M.E.N.O.S.seguranca;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Gera e confere o token de login (JWT assinado com HMAC-SHA256), usando só o que já vem no Java.
// O token leva: id, tipo, escolaId e a hora em que expira. Quem não tem o segredo não consegue forjar um.
public class TokenJwt {

    private static final String CABECALHO = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern ID = Pattern.compile("\"id\":(\\d+)");
    private static final Pattern TIPO = Pattern.compile("\"tipo\":\"(escola|aluno)\"");
    private static final Pattern ESCOLA = Pattern.compile("\"escolaId\":(\\d+)");
    private static final Pattern EXPIRA = Pattern.compile("\"exp\":(\\d+)");

    private static final Base64.Encoder CODIFICA = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODIFICA = Base64.getUrlDecoder();

    private final byte[] segredo;
    private final long validadeSegundos;

    public TokenJwt(byte[] segredo, long validadeSegundos) {
        this.segredo = segredo.clone();
        this.validadeSegundos = validadeSegundos;
    }

    public String gerar(Sessao sessao) {
        return gerar(sessao, Instant.now().getEpochSecond() + validadeSegundos);
    }

    // Separado para os testes poderem criar tokens já vencidos.
    String gerar(Sessao sessao, long expiraEm) {
        String cabecalho = codificar(CABECALHO);
        String corpo = codificar("{\"id\":" + sessao.id()
                + ",\"tipo\":\"" + sessao.tipo() + "\""
                + ",\"escolaId\":" + sessao.escolaId()
                + ",\"exp\":" + expiraEm + "}");
        String assinatura = CODIFICA.encodeToString(assinar(cabecalho + "." + corpo));
        return cabecalho + "." + corpo + "." + assinatura;
    }

    // Vazio quando o token é inválido, foi alterado ou já expirou.
    public Optional<Sessao> validar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String[] partes = token.split("\\.", -1);
        if (partes.length != 3 || !codificar(CABECALHO).equals(partes[0])) {
            return Optional.empty();
        }
        try {
            byte[] recebida = DECODIFICA.decode(partes[2]);
            byte[] esperada = assinar(partes[0] + "." + partes[1]);
            if (!MessageDigest.isEqual(recebida, esperada)) { // comparação em tempo constante
                return Optional.empty();
            }
            String corpo = new String(DECODIFICA.decode(partes[1]), StandardCharsets.UTF_8);
            Long id = numero(ID, corpo);
            Long escolaId = numero(ESCOLA, corpo);
            Long expira = numero(EXPIRA, corpo);
            Matcher tipo = TIPO.matcher(corpo);
            if (id == null || escolaId == null || expira == null || !tipo.find()
                    || expira <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(new Sessao(id, tipo.group(1), escolaId));
        } catch (IllegalArgumentException e) { // base64 inválido
            return Optional.empty();
        }
    }

    private static Long numero(Pattern padrao, String texto) {
        Matcher m = padrao.matcher(texto);
        return m.find() ? Long.valueOf(m.group(1)) : null;
    }

    private static String codificar(String texto) {
        return CODIFICA.encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] assinar(String texto) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo, "HmacSHA256"));
            return mac.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível assinar o token", e);
        }
    }
}
