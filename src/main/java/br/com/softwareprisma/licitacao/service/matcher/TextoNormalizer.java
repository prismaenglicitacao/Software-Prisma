package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class TextoNormalizer {

    public String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);

        texto = texto.replaceAll("\\p{M}", "");

        texto = texto.toLowerCase(Locale.ROOT);

        texto = texto.replace("²", "2").replace("³", "3");

        texto = texto.replaceAll("(?<=\\d)\\s+(?=[a-z])", "");
        texto = texto.replaceAll("(?<=[a-z])\\s+(?=\\d)", "");

        texto = texto.replaceAll("[^a-z0-9 ]", " ");

        texto = texto.replaceAll("\\s+", " ");

        return texto.trim();

    }

}