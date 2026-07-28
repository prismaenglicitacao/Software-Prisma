package br.com.softwareprisma.licitacao.service.matcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DescricaoMatcher {

    private final TextoNormalizer textoNormalizer;

    private final UnidadeNormalizer unidadeNormalizer;

    private final SimilaridadeCalculator similaridadeCalculator;

    private final ValorTecnicoExtractor valorTecnicoExtractor;

    private final AtributoQualitativoExtractor atributoQualitativoExtractor;

    public String gerarChave(String descricao, String unidade) {

        return textoNormalizer.normalizar(descricao)
                + "|"
                + unidadeNormalizer.normalizar(unidade);

    }

    public boolean corresponde(String a, String b) {
        return corresponde(a, "", b, "");
    }

    public boolean corresponde(
            String descricao1,
            String unidade1,
            String descricao2,
            String unidade2) {

        // unidades diferentes = nunca corresponde
        if (!unidadeNormalizer.normalizar(unidade1)
                .equals(unidadeNormalizer.normalizar(unidade2))) {
            return false;
        }

        // textos muito diferentes
        if (similaridade(descricao1, descricao2) < 0.80) {
            return false;
        }

        Set<String> valores1 = valorTecnicoExtractor.extrair(descricao1);
        Set<String> valores2 = valorTecnicoExtractor.extrair(descricao2);

        if (!valores1.equals(valores2)) {
            return false;
        }

        // Verificar atributos qualitativos críticos
        Set<String> atributos1 = atributoQualitativoExtractor.extrair(descricao1);
        Set<String> atributos2 = atributoQualitativoExtractor.extrair(descricao2);

        return atributos1.equals(atributos2);
    }

    public double similaridade(String descricao1, String descricao2) {

        Set<String> t1 = tokenizar(textoNormalizer.normalizar(descricao1));

        Set<String> t2 = tokenizar(textoNormalizer.normalizar(descricao2));

        return similaridadeCalculator.calcular(t1, t2);

    }

    private Set<String> tokenizar(String texto) {

        return Arrays.stream(texto.split(" "))

                .filter(token -> !token.isBlank())

                .filter(token -> !StopWords.PALAVRAS.contains(token))

                .map(token -> Sinonimos.MAPA.getOrDefault(token, token))

                .collect(Collectors.toSet());

    }

}