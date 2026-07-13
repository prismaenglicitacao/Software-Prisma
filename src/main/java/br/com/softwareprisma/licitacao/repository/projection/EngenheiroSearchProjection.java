package br.com.softwareprisma.licitacao.repository.projection;

import br.com.softwareprisma.licitacao.domain.enums.Area;

public record EngenheiroSearchProjection(
        Long id,
        String nome,
        Area area,
        int totalCats
) {
}
