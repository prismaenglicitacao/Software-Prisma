package br.com.softwareprisma.licitacao.service;

import java.math.BigDecimal;

public record RequisicaoAnalise(
        String descricao,
        String unidade,
        BigDecimal quantidade,
        String nomeCat
) {
    public RequisicaoAnalise(String descricao, String unidade, BigDecimal quantidade) {
        this(descricao, unidade, quantidade, null);
    }
}
