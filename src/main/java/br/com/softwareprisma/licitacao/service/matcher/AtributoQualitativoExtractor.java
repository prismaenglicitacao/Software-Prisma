package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AtributoQualitativoExtractor {

    private static final Pattern[] PADROES = {
        // período diurno / período noturno
        Pattern.compile("\\bperíodo\\s+(diurno|noturno)\\b", Pattern.CASE_INSENSITIVE),
        
        // dias úteis / sábado / domingo / feriado
        Pattern.compile("\\b(dias\\s+úteis|sábado|domingo|feriado)\\b", Pattern.CASE_INSENSITIVE),
        
        // baixa tensão / média tensão / alta tensão
        Pattern.compile("\\b(baixa|média|alta)\\s+tensão\\b", Pattern.CASE_INSENSITIVE),
        
        // concreto simples / concreto armado
        Pattern.compile("\\bconcreto\\s+(simples|armado)\\b", Pattern.CASE_INSENSITIVE),
        
        // DN específico (já capturado pelo ValorTecnicoExtractor, mas garantindo consistência)
        Pattern.compile("\\bdn\\s*(\\d+)\\b", Pattern.CASE_INSENSITIVE)
    };

    public Set<String> extrair(String descricao) {
        Set<String> atributos = new LinkedHashSet<>();
        
        if (descricao == null) {
            return atributos;
        }
        
        descricao = Normalizer.normalize(descricao, Normalizer.Form.NFD);
        descricao = descricao.replaceAll("\\p{M}", "");
        descricao = descricao.toLowerCase();
        
        for (Pattern pattern : PADROES) {
            Matcher matcher = pattern.matcher(descricao);
            while (matcher.find()) {
                atributos.add(
                    matcher.group()
                        .replaceAll("\\s+", "")
                        .toUpperCase()
                );
            }
        }
        
        return atributos;
    }
}
