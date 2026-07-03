package br.com.softwareprisma.licitacao.service.matcher;

import java.util.HashMap;
import java.util.Map;

public final class Sinonimos {

    private Sinonimos() {
    }

    public static final Map<String, String> MAPA = criar();

    private static Map<String, String> criar() {

        Map<String, String> mapa = new HashMap<>();

        mapa.put("watts", "w");
        mapa.put("watt", "w");

        mapa.put("luminaria", "luminaria");
        mapa.put("luminárias", "luminaria");

        mapa.put("tubulacao", "tubo");
        mapa.put("tubulação", "tubo");

        mapa.put("paralelepipedo", "paralelepipedo");
        mapa.put("paralelepipedos", "paralelepipedo");

        mapa.put("concreto", "concreto");

        mapa.put("cimento", "cimento");

        mapa.put("piso", "pavimento");

        mapa.put("piso intertravado", "pavimento");

        return mapa;

    }

}