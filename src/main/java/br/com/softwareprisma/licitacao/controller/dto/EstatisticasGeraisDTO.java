package br.com.softwareprisma.licitacao.controller.dto;

import java.math.BigDecimal;

public record EstatisticasGeraisDTO(
        Long quantidadeEngenheiros,
        Long quantidadeCats,
        Long quantidadeItens,
        Long quantidadeAnalises,
        Long quantidadeAnalisesAtenderam,
        Long quantidadeAnalisesNaoAtenderam,
        BigDecimal coberturaMedia
) {
}
