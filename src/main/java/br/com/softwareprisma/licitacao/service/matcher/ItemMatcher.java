package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

@Component
public class ItemMatcher {

    private final DescricaoMatcher descricaoMatcher;

    public ItemMatcher(DescricaoMatcher descricaoMatcher) {
        this.descricaoMatcher = descricaoMatcher;
    }

    public boolean corresponde(ItemAgrupado requisito,
                               ItemAgrupado cat) {

        if (!requisito.getUnidade().equalsIgnoreCase(cat.getUnidade())) {
            return false;
        }

        return descricaoMatcher.corresponde(
                requisito.getDescricao(),
                cat.getDescricao()
        );
    }
}