package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.form.CatItemLoteResultado;
import br.com.softwareprisma.licitacao.domain.CatItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CatItemLoteParser {

    public CatItemLoteResultado parse(String texto) {
        CatItemLoteResultado resultado = new CatItemLoteResultado();

        if (texto == null || texto.isBlank()) {
            resultado.setTotalItens(0);
            return resultado;
        }

        String[] linhas = texto.split("\n");
        resultado.setTotalItens(linhas.length);

        for (int i = 0; i < linhas.length; i++) {
            int numeroLinha = i + 1;
            String linha = linhas[i].trim();

            if (linha.isBlank()) {
                continue;
            }

            try {
                CatItem item = parseLinha(linha);
                resultado.getItensValidos().add(item);
                resultado.incrementarCadastrados();
            } catch (ParseException e) {
                resultado.adicionarErro(numeroLinha, e.getMessage());
            }
        }

        return resultado;
    }

    private CatItem parseLinha(String linha) throws ParseException {
        String separador = linha.contains("|") ? "\\|" : ";";
        String[] partes = linha.split(separador);

        if (partes.length != 3) {
            throw new ParseException("A linha deve conter exatamente 3 colunas separadas por | ou ;");
        }

        String descricao = partes[0].trim();
        String unidade = partes[1].trim();
        String quantidadeStr = partes[2].trim();

        if (descricao.isBlank()) {
            throw new ParseException("A descrição não pode estar vazia");
        }

        if (unidade.isBlank()) {
            throw new ParseException("A unidade não pode estar vazia");
        }

        if (quantidadeStr.isBlank()) {
            throw new ParseException("A quantidade não pode estar vazia");
        }

        BigDecimal quantidade;
        try {
            quantidade = new BigDecimal(quantidadeStr.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ParseException("A quantidade deve ser um número válido");
        }

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ParseException("A quantidade deve ser maior que zero");
        }

        CatItem item = new CatItem();
        item.setDescricao(descricao);
        item.setUnidade(unidade);
        item.setQuantidade(quantidade);

        return item;
    }

    private static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}
