package br.com.softwareprisma.licitacao.service.matcher;

import java.util.Set;

public final class StopWords {

    private StopWords() {
    }

    public static final Set<String> PALAVRAS = Set.of(
            "de",
            "da",
            "do",
            "das",
            "dos",
            "para",
            "com",
            "sem",
            "em",
            "na",
            "no",
            "e",
            "a",
            "o",
            "as",
            "os"
    );

}