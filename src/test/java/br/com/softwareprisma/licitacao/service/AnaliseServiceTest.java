package br.com.softwareprisma.licitacao.service;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnaliseServiceTest {

    @Test
    void deveReconhecerDescricaoPorPartesMesmoComFormatacaoDiferente() {
        var service = new AnaliseService(null, null);
        var descricaoCadastro = "Luminária led 90w";
        var descricaoBusca = "Luminária led - 90w";

        var chaveCadastro = service.normalizarChave(descricaoCadastro, "un");
        var chaveBusca = service.normalizarChave(descricaoBusca, "un");

        assertThat(service.mesmoMaterial(chaveCadastro, chaveBusca)).isTrue();
    }
}
