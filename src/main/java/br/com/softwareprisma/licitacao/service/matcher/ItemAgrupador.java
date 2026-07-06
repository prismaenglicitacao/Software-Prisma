package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ItemAgrupador {

    private final ItemMatcher matcher;

    public ItemAgrupador(ItemMatcher matcher) {
        this.matcher = matcher;
    }

    public void adicionar(List<ItemAgrupado> lista,
                          String descricao,
                          String unidade,
                          BigDecimal quantidade) {

        ItemAgrupado novo = new ItemAgrupado(
                descricao,
                unidade,
                quantidade
        );

        for (ItemAgrupado existente : lista) {

            if (matcher.corresponde(existente, novo)) {

                existente.adicionar(quantidade);
                return;

            }

        }

        lista.add(novo);

    }

    public List<ItemAgrupado> copiar(List<ItemAgrupado> origem){

        List<ItemAgrupado> copia = new ArrayList<>();

        for(ItemAgrupado item : origem){

            copia.add(item.copiar());

        }

        return copia;

    }

}