package br.com.softwareprisma.licitacao.repository.projection;

import br.com.softwareprisma.licitacao.domain.enums.Area;

public record ItemSearchProjection(
        Long id,
        Long catId,
        String descricao,
        String unidade,
        String catNome,
        String engenheiroNome,
        Area area
) {
}
