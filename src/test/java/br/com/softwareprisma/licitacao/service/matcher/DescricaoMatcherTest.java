package br.com.softwareprisma.licitacao.service.matcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DescricaoMatcherTest {

    @Autowired
    private DescricaoMatcher descricaoMatcher;

    @Test
    void deveNormalizarFormatosDeNumeros_DN100_vs_DN_100() {
        assertTrue(descricaoMatcher.corresponde("Eletroduto PVC DN100", "UN", "Eletroduto PVC DN 100", "UN"));
    }

    @Test
    void deveNormalizarFormatosDeNumeros_90W_vs_90_W() {
        assertTrue(descricaoMatcher.corresponde("Luminária LED 90W", "UN", "Luminária LED 90 W", "UN"));
    }

    @Test
    void deveNormalizarFormatosDeNumeros_PVC_DN100_vs_PVC_DN_100() {
        assertTrue(descricaoMatcher.corresponde("Tubo PVC DN100", "M", "Tubo PVC DN 100", "M"));
    }

    @Test
    void deveNormalizarFormatosDeNumeros_Luminaria_LED_120W_vs_Luminaria_Led_120_W() {
        assertTrue(descricaoMatcher.corresponde("Luminária LED 120W", "UN", "Luminária Led 120 W", "UN"));
    }

    @Test
    void naoDeveCorresponder_ValoresTecnicosDiferentes_DN100_vs_DN150() {
        assertFalse(descricaoMatcher.corresponde("Eletroduto DN100", "M", "Eletroduto DN150", "M"));
    }

    @Test
    void naoDeveCorresponder_ValoresTecnicosDiferentes_90W_vs_120W() {
        assertFalse(descricaoMatcher.corresponde("Luminária 90W", "UN", "Luminária 120W", "UN"));
    }

    @Test
    void naoDeveCorresponder_ObjetosDiferentes_Poste_vs_MeioFio() {
        assertFalse(descricaoMatcher.corresponde("Poste de concreto", "UN", "Meio-fio de concreto", "M"));
    }

    @Test
    void naoDeveCorresponder_ObjetosDiferentes_Sarjeta_vs_Poste() {
        assertFalse(descricaoMatcher.corresponde("Sarjeta em concreto", "M", "Poste de concreto", "UN"));
    }

    @Test
    void naoDeveCorresponder_ObjetosDiferentes_Transformador_vs_Luminaria() {
        assertFalse(descricaoMatcher.corresponde("Transformador 75kVA", "UN", "Luminária LED 90W", "UN"));
    }

    @Test
    void deveCorresponder_DescricoesIdenticas() {
        assertTrue(descricaoMatcher.corresponde("Luminária LED 90W", "UN", "Luminária LED 90W", "UN"));
    }

    @Test
    void naoDeveCorresponder_UnidadesDiferentes() {
        assertFalse(descricaoMatcher.corresponde("Luminária LED 90W", "UN", "Luminária LED 90W", "M"));
    }

    @Test
    void deveNormalizarUnidades_UN_vs_UNIDADE() {
        assertTrue(descricaoMatcher.corresponde("Luminária LED 90W", "UN", "Luminária LED 90W", "UNIDADE"));
    }

    @Test
    void deveNormalizarUnidades_M2_vs_M2() {
        assertTrue(descricaoMatcher.corresponde("Piso 50m2", "M2", "Piso 50 m2", "M²"));
    }
}
