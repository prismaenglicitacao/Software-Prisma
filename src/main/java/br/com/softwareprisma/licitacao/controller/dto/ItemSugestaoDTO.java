package br.com.softwareprisma.licitacao.controller.dto;

import java.math.BigDecimal;

public record ItemSugestaoDTO(
        String descricao,
        String unidade,
        BigDecimal quantidadeDisponivel
) {
    public ItemSugestaoDTO(String descricao, String unidade) {
        this(descricao, unidade, null);
    }
}
