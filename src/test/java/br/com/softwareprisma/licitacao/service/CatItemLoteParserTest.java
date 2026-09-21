package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.form.CatItemLoteResultado;
import br.com.softwareprisma.licitacao.domain.CatItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatItemLoteParserTest {

    private CatItemLoteParser parser;

    @BeforeEach
    void setUp() {
        parser = new CatItemLoteParser();
    }

    @Test
    void parse_TextoNulo_DeveRetornarResultadoVazio() {
        CatItemLoteResultado resultado = parser.parse(null);
        assertEquals(0, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertTrue(resultado.getErros().isEmpty());
    }

    @Test
    void parse_TextoVazio_DeveRetornarResultadoVazio() {
        CatItemLoteResultado resultado = parser.parse("");
        assertEquals(0, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertTrue(resultado.getErros().isEmpty());
    }

    @Test
    void parse_TextoEmBranco_DeveRetornarResultadoVazio() {
        CatItemLoteResultado resultado = parser.parse("   ");
        assertEquals(0, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertTrue(resultado.getErros().isEmpty());
    }

    @Test
    void parse_LinhaValidaComPipe_DeveParsearCorretamente() {
        String texto = "sarjeta | M | 1200";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(1, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertEquals(1, resultado.getItensValidos().size());

        CatItem item = resultado.getItensValidos().get(0);
        assertEquals("sarjeta", item.getDescricao());
        assertEquals("M", item.getUnidade());
        assertEquals(new BigDecimal("1200"), item.getQuantidade());
    }

    @Test
    void parse_LinhaValidaComPontoEVirgula_DeveParsearCorretamente() {
        String texto = "concreto;M³;350";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(1, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertEquals(1, resultado.getItensValidos().size());

        CatItem item = resultado.getItensValidos().get(0);
        assertEquals("concreto", item.getDescricao());
        assertEquals("M³", item.getUnidade());
        assertEquals(new BigDecimal("350"), item.getQuantidade());
    }

    @Test
    void parse_MultiplasLinhasValidas_DeveParsearTodas() {
        String texto = "escavação | M³ | 500\ntransporte | TON | 1200\nsarjeta | M | 1200";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(3, resultado.getTotalItens());
        assertEquals(3, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertEquals(3, resultado.getItensValidos().size());
    }

    @Test
    void parse_LinhaComDescricaoVazia_DeveGerarErroComConteudo() {
        String texto = "| M | 1200";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A descrição não pode estar vazia", erro.getMotivo());
        assertEquals("| M | 1200", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComUnidadeVazia_DeveGerarErroComConteudo() {
        String texto = "sarjeta |  | 1200";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A unidade não pode estar vazia", erro.getMotivo());
        assertEquals("sarjeta |  | 1200", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComQuantidadeVazia_DeveGerarErroComConteudo() {
        String texto = "sarjeta | M |";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A linha deve conter exatamente 3 colunas separadas por | ou ;", erro.getMotivo());
        assertEquals("sarjeta | M |", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComQuantidadeInvalida_DeveGerarErroComConteudo() {
        String texto = "sarjeta | M | abc";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A quantidade deve ser um número válido", erro.getMotivo());
        assertEquals("sarjeta | M | abc", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComQuantidadeZero_DeveGerarErroComConteudo() {
        String texto = "sarjeta | M | 0";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A quantidade deve ser maior que zero", erro.getMotivo());
        assertEquals("sarjeta | M | 0", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComQuantidadeNegativa_DeveGerarErroComConteudo() {
        String texto = "sarjeta | M | -50";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A quantidade deve ser maior que zero", erro.getMotivo());
        assertEquals("sarjeta | M | -50", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComColunasInsuficientes_DeveGerarErroComConteudo() {
        String texto = "sarjeta | M";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A linha deve conter exatamente 3 colunas separadas por | ou ;", erro.getMotivo());
        assertEquals("sarjeta | M", erro.getConteudoLinha());
    }

    @Test
    void parse_LinhaComColunasExcessivas_DeveGerarErroComConteudo() {
        String texto = "sarjeta | M | 1200 | extra";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(1, erro.getNumeroLinha());
        assertEquals("A linha deve conter exatamente 3 colunas separadas por | ou ;", erro.getMotivo());
        assertEquals("sarjeta | M | 1200 | extra", erro.getConteudoLinha());
    }

    @Test
    void parse_ErroNaLinha3_DeveIdentificarLinhaCorreta() {
        String texto = "escavação | M³ | 500\ntransporte | TON | 1200\nsarjeta | M | abc\nconcreto | M³ | 350";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(4, resultado.getTotalItens());
        assertEquals(3, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertEquals(3, resultado.getItensValidos().size());
        assertEquals(1, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro = resultado.getErros().get(0);
        assertEquals(3, erro.getNumeroLinha());
        assertEquals("sarjeta | M | abc", erro.getConteudoLinha());
    }

    @Test
    void parse_MultiplosErros_DeveIdentificarTodosComConteudos() {
        String texto = "| M | 1200\nsarjeta |  | 1200\nsarjeta | M | abc";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(3, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(3, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
        assertEquals(3, resultado.getErros().size());

        CatItemLoteResultado.ErroLinha erro1 = resultado.getErros().get(0);
        assertEquals(1, erro1.getNumeroLinha());
        assertEquals("| M | 1200", erro1.getConteudoLinha());

        CatItemLoteResultado.ErroLinha erro2 = resultado.getErros().get(1);
        assertEquals(2, erro2.getNumeroLinha());
        assertEquals("sarjeta |  | 1200", erro2.getConteudoLinha());

        CatItemLoteResultado.ErroLinha erro3 = resultado.getErros().get(2);
        assertEquals(3, erro3.getNumeroLinha());
        assertEquals("sarjeta | M | abc", erro3.getConteudoLinha());
    }

    @Test
    void parse_LinhaEmBranco_DeveIgnorar() {
        String texto = "sarjeta | M | 1200\n\nconcreto | M³ | 350";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(3, resultado.getTotalItens());
        assertEquals(2, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertEquals(2, resultado.getItensValidos().size());
    }

    @Test
    void parse_QuantidadeComVirgula_DeveConverterParaPonto() {
        String texto = "sarjeta | M | 1200,50";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(1, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());

        CatItem item = resultado.getItensValidos().get(0);
        assertEquals(new BigDecimal("1200.50"), item.getQuantidade());
    }

    @Test
    void parse_LinhaValidaComEspacosExtras_DeveTrimarCorretamente() {
        String texto = "  sarjeta   |   M   |   1200  ";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(1, resultado.getTotalItens());
        assertEquals(1, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());

        CatItem item = resultado.getItensValidos().get(0);
        assertEquals("sarjeta", item.getDescricao());
        assertEquals("M", item.getUnidade());
        assertEquals(new BigDecimal("1200"), item.getQuantidade());
    }

    @Test
    void parse_LoteComSucesso_DeveRetornarItensValidosParaFeedback() {
        String texto = "sarjeta | M | 1200\nconcreto | M³ | 350\nescavação | M³ | 500";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(3, resultado.getTotalItens());
        assertEquals(3, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertEquals(3, resultado.getItensValidos().size());

        List<CatItem> itens = resultado.getItensValidos();
        assertEquals("sarjeta", itens.get(0).getDescricao());
        assertEquals("M", itens.get(0).getUnidade());
        assertEquals(new BigDecimal("1200"), itens.get(0).getQuantidade());

        assertEquals("concreto", itens.get(1).getDescricao());
        assertEquals("M³", itens.get(1).getUnidade());
        assertEquals(new BigDecimal("350"), itens.get(1).getQuantidade());

        assertEquals("escavação", itens.get(2).getDescricao());
        assertEquals("M³", itens.get(2).getUnidade());
        assertEquals(new BigDecimal("500"), itens.get(2).getQuantidade());
    }

    @Test
    void parse_LoteComErros_DeveRetornarApenasItensValidosParaFeedback() {
        String texto = "sarjeta | M | 1200\nitem invalido\nconcreto | M³ | 350";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(3, resultado.getTotalItens());
        assertEquals(2, resultado.getItensCadastrados());
        assertEquals(1, resultado.getItensComErro());
        assertEquals(2, resultado.getItensValidos().size());

        List<CatItem> itens = resultado.getItensValidos();
        assertEquals("sarjeta", itens.get(0).getDescricao());
        assertEquals("concreto", itens.get(1).getDescricao());
    }

    @Test
    void parse_LoteVazio_DeveRetornarListaVaziaParaFeedback() {
        String texto = "";
        CatItemLoteResultado resultado = parser.parse(texto);

        assertEquals(0, resultado.getTotalItens());
        assertEquals(0, resultado.getItensCadastrados());
        assertEquals(0, resultado.getItensComErro());
        assertTrue(resultado.getItensValidos().isEmpty());
    }
}
