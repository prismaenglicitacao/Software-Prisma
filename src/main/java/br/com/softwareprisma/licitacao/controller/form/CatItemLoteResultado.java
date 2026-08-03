package br.com.softwareprisma.licitacao.controller.form;

import br.com.softwareprisma.licitacao.domain.CatItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CatItemLoteResultado {

    private int totalItens;
    private int itensCadastrados;
    private int itensComErro;
    private List<ErroLinha> erros;
    private List<CatItem> itensValidos;

    public CatItemLoteResultado() {
        this.erros = new ArrayList<>();
        this.itensValidos = new ArrayList<>();
    }

    public void adicionarErro(int numeroLinha, String motivo) {
        this.erros.add(new ErroLinha(numeroLinha, motivo));
        this.itensComErro++;
    }

    public void incrementarCadastrados() {
        this.itensCadastrados++;
    }

    public void setTotalItens(int total) {
        this.totalItens = total;
    }

    @Getter
    @AllArgsConstructor
    public static class ErroLinha {
        private final int numeroLinha;
        private final String motivo;
    }
}
