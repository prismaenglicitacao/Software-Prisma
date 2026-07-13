package br.com.softwareprisma.licitacao.repository.projection;

public record CatSearchProjection(
        Long id,
        String nome,
        String engenheiroNome,
        int totalItens
) {
}
