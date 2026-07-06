package br.com.softwareprisma.licitacao.service.matcher;

import java.math.BigDecimal;

public class ItemAgrupado {

    private final String descricao;
    private final String unidade;
    private BigDecimal quantidade;

    public ItemAgrupado(String descricao,
                        String unidade,
                        BigDecimal quantidade) {

        this.descricao = descricao;
        this.unidade = unidade;
        this.quantidade = quantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void adicionar(BigDecimal valor) {
        quantidade = quantidade.add(valor);
    }

    public void remover(BigDecimal valor) {
        quantidade = quantidade.subtract(valor);
    }

    public ItemAgrupado copiar() {
        return new ItemAgrupado(
                descricao,
                unidade,
                quantidade
        );
    }
}
