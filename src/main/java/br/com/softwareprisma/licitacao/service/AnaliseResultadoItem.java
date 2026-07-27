package br.com.softwareprisma.licitacao.service;

import java.math.BigDecimal;
import java.util.List;

public record AnaliseResultadoItem(
        String descricao,
        String unidade,
        BigDecimal exigido,
        BigDecimal encontrado,
        boolean atende,
        List<String> origens
) {
}
