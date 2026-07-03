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

    public boolean corresponde(String descricao1, String unidade1, String descricao2, String unidade2) {
        String unidadeNormalizada1 = unidadeNormalizer.normalizar(unidade1);
        String unidadeNormalizada2 = unidadeNormalizer.normalizar(unidade2);
        
        if (!unidadeNormalizada1.equals(unidadeNormalizada2)) {
            return false;
        }
        
        // Prioridade 1: Comparar valores técnicos
        var valores1 = valorTecnicoExtractor.extrair(descricao1);
        var valores2 = valorTecnicoExtractor.extrair(descricao2);
        
        // Se ambos têm valores técnicos, eles devem ser idênticos
        if (!valores1.isEmpty() && !valores2.isEmpty()) {
            if (!valores1.equals(valores2)) {
                return false;
            }
        }
        
        // Prioridade 2: Verificar substantivos principais (evitar falsos positivos)
        if (!substantivosPrincipaisCorrespondem(descricao1, descricao2)) {
            return false;
        }
        
        // Prioridade 3: Comparar similaridade textual
        return similaridade(descricao1, descricao2) >= 0.80;
    }

    public boolean corresponde(String descricao1, String descricao2) {

        return similaridade(descricao1, descricao2) >= 0.80;

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

    private boolean substantivosPrincipaisCorrespondem(String descricao1, String descricao2) {
        var tokens1 = tokenizar(textoNormalizer.normalizar(descricao1));
        var tokens2 = tokenizar(textoNormalizer.normalizar(descricao2));
        
        // Se não há tokens suficientes, não pode validar
        if (tokens1.size() < 2 || tokens2.size() < 2) {
            return true;
        }
        
        // Verificar se há pelo menos um substantivo principal em comum
        // (excluindo adjetivos genéricos como "concreto", "pvc", "led")
        Set<String> adjetivosGenericos = Set.of("concreto", "pvc", "led", "metal", "plastico", "madeira", "ferro", "aco");
        
        var substantivos1 = tokens1.stream()
                .filter(t -> !adjetivosGenericos.contains(t))
                .collect(Collectors.toSet());
        
        var substantivos2 = tokens2.stream()
                .filter(t -> !adjetivosGenericos.contains(t))
                .collect(Collectors.toSet());
        
        // Se ambos têm substantivos principais, devem ter pelo menos um em comum
        if (!substantivos1.isEmpty() && !substantivos2.isEmpty()) {
            substantivos1.retainAll(substantivos2);
            if (substantivos1.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

}