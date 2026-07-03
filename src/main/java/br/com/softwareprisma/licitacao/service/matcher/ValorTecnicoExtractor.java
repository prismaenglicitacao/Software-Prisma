package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ValorTecnicoExtractor {

    private static final Pattern[] PADROES = {

            // 90W
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*w\\b", Pattern.CASE_INSENSITIVE),

            // 220V
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*v\\b", Pattern.CASE_INSENSITIVE),

            // 10A
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*a\\b", Pattern.CASE_INSENSITIVE),

            // DN100
            Pattern.compile("\\bdn\\s*\\d+\\b", Pattern.CASE_INSENSITIVE),

            // FCK25
            Pattern.compile("\\bfck\\s*\\d+\\b", Pattern.CASE_INSENSITIVE),

            // Ø100
            Pattern.compile("ø\\s*\\d+", Pattern.CASE_INSENSITIVE),

            // 150mm
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*mm\\b", Pattern.CASE_INSENSITIVE),

            // 3cm
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*cm\\b", Pattern.CASE_INSENSITIVE),

            // 5m
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*m\\b", Pattern.CASE_INSENSITIVE),

            // 2,5kg
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*kg\\b", Pattern.CASE_INSENSITIVE),

            // 50x50
            Pattern.compile("\\b\\d+\\s*x\\s*\\d+\\b", Pattern.CASE_INSENSITIVE),

            // 40X60
            Pattern.compile("\\b\\d+\\s*X\\s*\\d+\\b"),

            // 5CV
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*cv\\b", Pattern.CASE_INSENSITIVE),

            // 15HP
            Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\s*hp\\b", Pattern.CASE_INSENSITIVE)

    };

    public Set<String> extrair(String descricao) {

        Set<String> valores = new LinkedHashSet<>();

        if (descricao == null) {
            return valores;
        }

        descricao = Normalizer.normalize(descricao, Normalizer.Form.NFD);

        descricao = descricao.replaceAll("\\p{M}", "");

        descricao = descricao.toLowerCase();

        for (Pattern pattern : PADROES) {

            Matcher matcher = pattern.matcher(descricao);

            while (matcher.find()) {

                valores.add(
                        matcher.group()
                                .replaceAll("\\s+", "")
                                .toUpperCase()
                );

            }

        }

        return valores;

    }

}