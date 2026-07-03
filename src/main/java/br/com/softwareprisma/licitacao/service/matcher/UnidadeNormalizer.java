package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

@Component
public class UnidadeNormalizer {

    public String normalizar(String unidade) {

        if (unidade == null) {
            return "";
        }

        unidade = unidade.trim().toLowerCase();

        return switch (unidade) {

            case "un", "und", "unidade" -> "UN";

            case "m²", "m2" -> "M2";

            case "m³", "m3" -> "M3";

            case "kg", "quilo", "quilograma" -> "KG";

            case "m" -> "M";

            default -> unidade.toUpperCase();

        };

    }

}