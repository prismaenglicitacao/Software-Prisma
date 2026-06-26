package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;

import java.math.BigDecimal;
import java.util.List;

public record AnaliseResultado(
        ResultadoAnalise resultado,
        List<String> engenheiros,
        List<String> cats,
        List<AnaliseResultadoItem> itens,
        List<String> itensFaltantes,
        BigDecimal cobertura,
        boolean atende
) {
}
