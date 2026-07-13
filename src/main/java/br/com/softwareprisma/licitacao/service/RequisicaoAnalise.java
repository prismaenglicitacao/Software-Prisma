package br.com.softwareprisma.licitacao.service;

import java.math.BigDecimal;

public record RequisicaoAnalise(
        String descricao,
        String unidade,
        BigDecimal quantidade
) {
}