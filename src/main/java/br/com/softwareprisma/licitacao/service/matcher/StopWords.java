package br.com.softwareprisma.licitacao.service.matcher;

import java.util.Set;

public final class StopWords {

    private StopWords() {
    }

    public static final Set<String> PALAVRAS = Set.of(
        "a", "o", "as", "os", "um", "uma", "uns", "umas",
        "de", "do", "da", "dos", "das", "em", "no", "na", "nos", "nas",
        "para", "por", "com", "sem", "sob", "sobre",
        "e", "ou", "mas", "que", "quem", "qual", "quais",
        "como", "onde", "quando", "porque", "pois",
        "este", "esta", "isto", "esse", "essa", "isso",
        "aquele", "aquela", "aquilo",
        "meu", "minha", "teu", "tua", "seu", "sua",
        "nossos", "nossas", "vossos", "vossas",
        "muito", "muita", "pouco", "pouca",
        "mais", "menos", "tão",
        "já", "ainda", "apenas", "só",
        "também", "nem", "sequer"
    );
}
