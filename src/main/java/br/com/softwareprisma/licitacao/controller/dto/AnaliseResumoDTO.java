package br.com.softwareprisma.licitacao.controller.dto;

import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AnaliseResumoDTO(
        Long id,
        Area area,
        LocalDateTime dataCriacao,
        ResultadoAnalise resultado,
        BigDecimal cobertura
) {
}
