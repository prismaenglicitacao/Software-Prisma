package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemLocalizador {

    private final ItemMatcher matcher;

    public ItemLocalizador(ItemMatcher matcher) {
        this.matcher = matcher;
    }

    public ItemAgrupado localizar(ItemAgrupado requisito,
                                  List<ItemAgrupado> lista){

        for(ItemAgrupado item : lista){

            if(matcher.corresponde(requisito,item)){

                return item;

            }

        }

        return null;

    }

}