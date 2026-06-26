package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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
