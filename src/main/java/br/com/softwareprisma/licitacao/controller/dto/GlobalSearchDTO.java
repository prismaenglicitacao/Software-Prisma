package br.com.softwareprisma.licitacao.controller.dto;

import java.util.List;

public record GlobalSearchDTO(
        List<EngenheiroResultado> engenheiros,
        List<CatResultado> cats,
        List<ItemResultado> itens,
        boolean temMaisResultados
) {
    public record EngenheiroResultado(
            Long id,
            String nome,
            String area,
            int totalCats
    ) {
        public String tipo() {
            return "ENGENHEIRO";
        }
    }

    public record CatResultado(
            Long id,
            String nome,
            String engenheiroNome,
            int totalItens
    ) {
        public String tipo() {
            return "CAT";
        }
    }

    public record ItemResultado(
            Long id,
            Long catId,
            String descricao,
            String unidade,
            String catNome,
            String engenheiroNome,
            String area
    ) {
        public String tipo() {
            return "ITEM";
        }
    }
}
