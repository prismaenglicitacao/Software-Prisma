package br.com.softwareprisma.licitacao.service.matcher;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValorTecnicoExtractorTest {

    private final ValorTecnicoExtractor extractor = new ValorTecnicoExtractor();

    @Test
    void deveNormalizarDN100_vs_DN_100() {
        Set<String> valores1 = extractor.extrair("Eletroduto DN100");
        Set<String> valores2 = extractor.extrair("Eletroduto DN 100");
        assertEquals(valores1, valores2);
    }

    @Test
    void deveNormalizar90W_vs_90_W() {
        Set<String> valores1 = extractor.extrair("Luminária LED 90W");
        Set<String> valores2 = extractor.extrair("Luminária LED 90 W");
        assertEquals(valores1, valores2);
    }

    @Test
    void deveNormalizar120W_vs_120_W() {
        Set<String> valores1 = extractor.extrair("Luminária LED 120W");
        Set<String> valores2 = extractor.extrair("Luminária LED 120 W");
        assertEquals(valores1, valores2);
    }

    @Test
    void naoDeveSerIguais_DN100_vs_DN150() {
        Set<String> valores1 = extractor.extrair("Eletroduto DN100");
        Set<String> valores2 = extractor.extrair("Eletroduto DN150");
        assertNotEquals(valores1, valores2);
    }

    @Test
    void naoDeveSerIguais_90W_vs_120W() {
        Set<String> valores1 = extractor.extrair("Luminária 90W");
        Set<String> valores2 = extractor.extrair("Luminária 120W");
        assertNotEquals(valores1, valores2);
    }

    @Test
    void deveExtrairMultiplosValoresTecnicos() {
        Set<String> valores = extractor.extrair("Luminária LED 90W 220V");
        assertTrue(valores.contains("90W"));
        assertTrue(valores.contains("220V"));
    }

    @Test
    void deveExtrairValorTecnico_MM() {
        Set<String> valores1 = extractor.extrair("Tubo 150mm");
        Set<String> valores2 = extractor.extrair("Tubo 150 mm");
        assertEquals(valores1, valores2);
    }

    @Test
    void deveExtrairValorTecnico_CM() {
        Set<String> valores = extractor.extrair("Placa 3cm");
        assertTrue(valores.contains("3CM"));
    }

    @Test
    void deveExtrairValorTecnico_M() {
        Set<String> valores1 = extractor.extrair("Poste 12m");
        Set<String> valores2 = extractor.extrair("Poste 12 m");
        assertEquals(valores1, valores2);
    }

    @Test
    void deveExtrairValorTecnico_FCK() {
        Set<String> valores1 = extractor.extrair("Concreto FCK25");
        Set<String> valores2 = extractor.extrair("Concreto FCK 25");
        assertEquals(valores1, valores2);
    }

    @Test
    void deveExtrairValorTecnico_Diametro() {
        Set<String> valores1 = extractor.extrair("Tubo Ø100");
        Set<String> valores2 = extractor.extrair("Tubo Ø 100");
        assertEquals(valores1, valores2);
    }

    @Test
    void deveExtrairValorTecnico_Dimensao() {
        Set<String> valores1 = extractor.extrair("Placa 50x50");
        Set<String> valores2 = extractor.extrair("Placa 50 x 50");
        assertEquals(valores1, valores2);
    }
}
