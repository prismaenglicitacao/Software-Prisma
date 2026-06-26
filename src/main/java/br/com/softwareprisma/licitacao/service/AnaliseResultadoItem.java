package br.com.softwareprisma.licitacao.service;

import java.math.BigDecimal;

public record AnaliseResultadoItem(
        String descricao,
        String unidade,
        BigDecimal exigido,
        BigDecimal encontrado,
        boolean atende
) {
}
