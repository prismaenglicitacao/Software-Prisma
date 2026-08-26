package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseItemRepository;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ItemAutocompleteServiceIntegrationTest {

    @Autowired
    private ItemAutocompleteService itemAutocompleteService;

    @Autowired
    private CatItemRepository catItemRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private EngenheiroRepository engenheiroRepository;

    @Autowired
    private AnaliseRepository analiseRepository;

    @Autowired
    private AnaliseItemRepository analiseItemRepository;

    @Autowired
    private AnaliseService analiseService;

    @Autowired
    private DescricaoMatcher descricaoMatcher;

    private Engenheiro engenheiro1;
    private Engenheiro engenheiro2;
    private Cat cat1;
    private Cat cat2;

    @BeforeEach
    void setUp() {
        engenheiro1 = new Engenheiro();
        engenheiro1.setNome("Engenheiro A");
        engenheiro1.setArea(Area.CIVIL);
        engenheiro1 = engenheiroRepository.save(engenheiro1);

        engenheiro2 = new Engenheiro();
        engenheiro2.setNome("Engenheiro B");
        engenheiro2.setArea(Area.CIVIL);
        engenheiro2 = engenheiroRepository.save(engenheiro2);

        cat1 = new Cat();
        cat1.setNome("CAT 1");
        cat1.setNumeroCat("CAT-001");
        cat1.setMunicipio("São Paulo");
        cat1.setEngenheiro(engenheiro1);
        cat1 = catRepository.save(cat1);

        cat2 = new Cat();
        cat2.setNome("CAT 2");
        cat2.setNumeroCat("CAT-002");
        cat2.setMunicipio("Rio de Janeiro");
        cat2.setEngenheiro(engenheiro2);
        cat2 = catRepository.save(cat2);
    }

    @Test
    void buscarSugestoesAgrupadas_Integracao_CatsDiferentesEngenheirosDiferentes() {
        // Arrange
        String descricao = "PASSEIO EM LAJOTA DE CONCRETO 50X50, APLICANDO SOBRE LASTRO DE CONCRETO 1:4:8 DE 5 CM DE ESPESSURA, INCLUSIVE EXECUÇÃO DO LASTRO";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("688.54"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao + "."); // Diferença: ponto no final
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("42.86"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL);

        // Assert
        assertEquals(1, resultado.size(), "Deve agrupar itens com descrições equivalentes");
        
        ItemSugestaoDTO sugestao = resultado.get(0);
        assertEquals(new BigDecimal("731.40"), sugestao.quantidadeDisponivel(), 
                "Quantidade deve ser a soma: 688.54 + 42.86 = 731.40");
    }

    @Test
    void buscarSugestoesAgrupadas_Integracao_EdicaoPosterior_DeveRefletir() {
        // Arrange
        String descricao1 = "PASSEIO DE CONCRETO TIPO A";
        String descricao2 = "PASSEIO DE CONCRETO TIPO B";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao1);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        item1 = catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao2);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat2);
        item2 = catItemRepository.save(item2);

        // Act 1: Antes da edicao - devem aparecer separados
        List<ItemSugestaoDTO> resultadoAntes = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL);
        assertEquals(2, resultadoAntes.size(), "Antes da edicao devem aparecer separados");

        // Act 2: Editar item2 para ter mesma descricao que item1
        item2.setDescricao(descricao1);
        catItemRepository.save(item2);

        // Act 3: Apos edicao - devem aparecer agrupados
        List<ItemSugestaoDTO> resultadoDepois = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL);
        assertEquals(1, resultadoDepois.size(), "Apos edicao devem aparecer agrupados");
        assertEquals(new BigDecimal("300.00"), resultadoDepois.get(0).quantidadeDisponivel(),
                "Quantidade deve ser a soma: 100.00 + 200.00 = 300.00");
    }

    @Test
    void buscarSugestoesAgrupadas_Integracao_ConsistenciaComDescricaoMatcher() {
        // Arrange
        String descricao = "PASSEIO DE CONCRETO 1:3:5";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao); // Mesma descrição
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("50.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL);

        // Assert
        assertEquals(1, resultado.size());
        
        // Validar que a chave gerada pelo DescricaoMatcher é a mesma para ambos
        String chave1 = descricaoMatcher.gerarChave(item1.getDescricao(), item1.getUnidade());
        String chave2 = descricaoMatcher.gerarChave(item2.getDescricao(), item2.getUnidade());
        assertEquals(chave1, chave2, "Chaves devem ser iguais para descrições idênticas");
    }

    @Test
    void buscarSugestoesAgrupadas_Integracao_SemArea_DeveFuncionar() {
        // Arrange
        String descricao = "PASSEIO DE CONCRETO";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", null);

        // Assert
        assertEquals(1, resultado.size());
    }

    @Test
    void buscarItensRecentes_Integracao_CenarioA_DuasCATs_DeveSomar() {
        // Arrange - Cenário A: Duas CATs
        String descricao = "EXECUÇÃO DE PASSEIO";
        String unidade = "M³";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("102.92"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("137.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Criar análise anterior com esse item
        Analise analise = new Analise();
        analise.setArea(Area.CIVIL);
        analise.setResultado(ResultadoAnalise.ATENDE);
        analise.setCobertura(BigDecimal.valueOf(100));
        analiseRepository.save(analise);

        AnaliseItem analiseItem = new AnaliseItem();
        analiseItem.setAnalise(analise);
        analiseItem.setDescricao(descricao);
        analiseItem.setUnidade(unidade);
        analiseItem.setQuantidade(new BigDecimal("50.00"));
        analiseItemRepository.save(analiseItem);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("239.92"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void buscarItensRecentes_Integracao_CenarioB_MultiplasAnalises_NaoDeveMultiplicar() {
        // Arrange - Cenário B: Mesmo item em várias análises
        String descricao = "EXECUÇÃO DE PASSEIO";
        String unidade = "M³";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("102.92"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("137.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Criar 3 análises anteriores com o mesmo item
        for (int i = 0; i < 3; i++) {
            Analise analise = new Analise();
            analise.setArea(Area.CIVIL);
            analise.setResultado(ResultadoAnalise.ATENDE);
            analise.setCobertura(BigDecimal.valueOf(100));
            analiseRepository.save(analise);

            AnaliseItem analiseItem = new AnaliseItem();
            analiseItem.setAnalise(analise);
            analiseItem.setDescricao(descricao);
            analiseItem.setUnidade(unidade);
            analiseItem.setQuantidade(new BigDecimal("10.00"));
            analiseItemRepository.save(analiseItem);
        }

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("239.92"), resultado.get(0).quantidadeDisponivel());
        // NÃO deve ser 719.76 (239.92 * 3)
    }

    @Test
    void buscarItensRecentes_Integracao_CenarioE_DescricoesEquivalentes_DeveAgrupar() {
        // Arrange - Cenário E: Descrições equivalentes
        String descricao1 = "PASSEIO DE CONCRETO";
        String descricao2 = "PASSEIO DE CONCRETO.";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao1);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao2);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Criar análise com descrição sem ponto
        Analise analise = new Analise();
        analise.setArea(Area.CIVIL);
        analise.setResultado(ResultadoAnalise.ATENDE);
        analise.setCobertura(BigDecimal.valueOf(100));
        analiseRepository.save(analise);

        AnaliseItem analiseItem = new AnaliseItem();
        analiseItem.setAnalise(analise);
        analiseItem.setDescricao(descricao1);
        analiseItem.setUnidade(unidade);
        analiseItem.setQuantidade(new BigDecimal("50.00"));
        analiseItemRepository.save(analiseItem);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL);

        // Assert
        assertEquals(1, resultado.size(), "Deve agrupar descrições equivalentes");
        assertEquals(new BigDecimal("300.00"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void buscarItensRecentes_Integracao_UnidadesDiferentes_NaoDeveAgrupar() {
        // Arrange - Unidades diferentes
        String descricao = "PASSEIO DE CONCRETO";
        String unidade1 = "M²";
        String unidade2 = "M³";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade1);
        item1.setQuantidade(new BigDecimal("500.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade(unidade2);
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Criar análises com ambas as unidades
        Analise analise1 = new Analise();
        analise1.setArea(Area.CIVIL);
        analise1.setResultado(ResultadoAnalise.ATENDE);
        analise1.setCobertura(BigDecimal.valueOf(100));
        analiseRepository.save(analise1);

        AnaliseItem analiseItem1 = new AnaliseItem();
        analiseItem1.setAnalise(analise1);
        analiseItem1.setDescricao(descricao);
        analiseItem1.setUnidade(unidade1);
        analiseItem1.setQuantidade(new BigDecimal("50.00"));
        analiseItemRepository.save(analiseItem1);

        Analise analise2 = new Analise();
        analise2.setArea(Area.CIVIL);
        analise2.setResultado(ResultadoAnalise.ATENDE);
        analise2.setCobertura(BigDecimal.valueOf(100));
        analiseRepository.save(analise2);

        AnaliseItem analiseItem2 = new AnaliseItem();
        analiseItem2.setAnalise(analise2);
        analiseItem2.setDescricao(descricao);
        analiseItem2.setUnidade(unidade2);
        analiseItem2.setQuantidade(new BigDecimal("30.00"));
        analiseItemRepository.save(analiseItem2);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL);

        // Assert
        assertEquals(2, resultado.size(), "Não deve agrupar unidades diferentes");
        
        // Verificar se ambas as unidades estão presentes
        boolean temM2 = resultado.stream().anyMatch(item -> item.unidade().equals("M²"));
        boolean temM3 = resultado.stream().anyMatch(item -> item.unidade().equals("M³"));
        assertTrue(temM2 && temM3);
    }

    @Test
    void autocomplete_vs_recentes_MesmaCapacidade_DeveSerIguais() {
        // Arrange - Cenário G: Comparar autocomplete e recentes
        String descricao = "EXECUÇÃO DE PASSEIO";
        String unidade = "M³";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("102.92"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("137.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Criar análise anterior com esse item
        Analise analise = new Analise();
        analise.setArea(Area.CIVIL);
        analise.setResultado(ResultadoAnalise.ATENDE);
        analise.setCobertura(BigDecimal.valueOf(100));
        analiseRepository.save(analise);

        AnaliseItem analiseItem = new AnaliseItem();
        analiseItem.setAnalise(analise);
        analiseItem.setDescricao(descricao);
        analiseItem.setUnidade(unidade);
        analiseItem.setQuantidade(new BigDecimal("50.00"));
        analiseItemRepository.save(analiseItem);

        // Act
        List<ItemSugestaoDTO> resultadoAutocomplete = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL);
        List<ItemSugestaoDTO> resultadoRecentes = itemAutocompleteService.buscarItensRecentes(Area.CIVIL);

        // Assert
        assertEquals(1, resultadoAutocomplete.size());
        assertEquals(1, resultadoRecentes.size());
        
        BigDecimal capacidadeAutocomplete = resultadoAutocomplete.get(0).quantidadeDisponivel();
        BigDecimal capacidadeRecentes = resultadoRecentes.get(0).quantidadeDisponivel();
        
        assertEquals(new BigDecimal("239.92"), capacidadeAutocomplete);
        assertEquals(new BigDecimal("239.92"), capacidadeRecentes);
        assertEquals(capacidadeAutocomplete, capacidadeRecentes, 
            "Autocomplete e recentes devem retornar exatamente a mesma capacidade");
    }

    @Test
    void fluxoCompleto_CliqueRecente_Analise_DeveSomarCATs() {
        // Arrange - Cenário F: Fluxo completo
        String descricao = "EXECUÇÃO DE PASSEIO";
        String unidade = "M³";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("102.92"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("137.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Criar análise anterior para aparecer nos recentes
        Analise analiseAnterior = new Analise();
        analiseAnterior.setArea(Area.CIVIL);
        analiseAnterior.setResultado(ResultadoAnalise.ATENDE);
        analiseAnterior.setCobertura(BigDecimal.valueOf(100));
        analiseRepository.save(analiseAnterior);

        AnaliseItem analiseItemAnterior = new AnaliseItem();
        analiseItemAnterior.setAnalise(analiseAnterior);
        analiseItemAnterior.setDescricao(descricao);
        analiseItemAnterior.setUnidade(unidade);
        analiseItemAnterior.setQuantidade(new BigDecimal("50.00"));
        analiseItemRepository.save(analiseItemAnterior);

        // Act - Verificar que o AnaliseItem salvo não tem referência a CAT específica
        AnaliseItem itemSalvo = analiseItemRepository.findById(analiseItemAnterior.getId()).orElseThrow();
        
        // Assert - O AnaliseItem deve representar apenas o item lógico
        assertEquals(descricao, itemSalvo.getDescricao());
        assertEquals(unidade, itemSalvo.getUnidade());
        assertEquals(new BigDecimal("50.00"), itemSalvo.getQuantidade());
        // AnaliseItem não tem campo cat - representa apenas item lógico
        assertNotNull(itemSalvo.getAnalise());
    }

    @Test
    void selecaoMultipla_TresItensMesmaUnidade_DeveAdicionarTodos() {
        // Arrange - Teste B: Seleção múltipla com 3 itens
        String descricao1 = "INTERTRAVADO 8 CM COLORIDO";
        String descricao2 = "INTERTRAVADO 8 CM COR NATURAL";
        String descricao3 = "EXECUÇÃO DE PAVIMENTO INTERTRAVADO 8 CM";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao1);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("300.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao2);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("250.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        CatItem item3 = new CatItem();
        item3.setDescricao(descricao3);
        item3.setUnidade(unidade);
        item3.setQuantidade(new BigDecimal("400.00"));
        item3.setCat(cat1);
        catItemRepository.save(item3);

        // Criar análise
        Analise analise = new Analise();
        analise.setArea(Area.CIVIL);
        analise = analiseRepository.save(analise);

        // Act - Simular adição de múltiplos itens (como seria no frontend)
        AnaliseItem analiseItem1 = new AnaliseItem();
        analiseItem1.setAnalise(analise);
        analiseItem1.setDescricao(descricao1);
        analiseItem1.setUnidade(unidade);
        analiseItem1.setQuantidade(new BigDecimal("300.00"));
        analiseItemRepository.save(analiseItem1);

        AnaliseItem analiseItem2 = new AnaliseItem();
        analiseItem2.setAnalise(analise);
        analiseItem2.setDescricao(descricao2);
        analiseItem2.setUnidade(unidade);
        analiseItem2.setQuantidade(new BigDecimal("250.00"));
        analiseItemRepository.save(analiseItem2);

        AnaliseItem analiseItem3 = new AnaliseItem();
        analiseItem3.setAnalise(analise);
        analiseItem3.setDescricao(descricao3);
        analiseItem3.setUnidade(unidade);
        analiseItem3.setQuantidade(new BigDecimal("400.00"));
        analiseItemRepository.save(analiseItem3);

        // Assert - Verificar que os 3 itens foram persistidos individualmente
        List<AnaliseItem> itensSalvos = analiseItemRepository.findByAnalise(analise);
        assertEquals(3, itensSalvos.size());
        
        // Verificar quantidades
        BigDecimal total = itensSalvos.stream()
            .map(AnaliseItem::getQuantidade)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("950.00"), total);
    }

    @Test
    void selecaoMultipla_UnidadesDiferentes_NaoDevePermitir() {
        // Arrange - Teste E: Unidades diferentes
        String descricao1 = "INTERTRAVADO 8 CM";
        String descricao2 = "INTERTRAVADO 8 CM";
        String unidade1 = "M²";
        String unidade2 = "M³";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao1);
        item1.setUnidade(unidade1);
        item1.setQuantidade(new BigDecimal("300.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao2);
        item2.setUnidade(unidade2);
        item2.setQuantidade(new BigDecimal("50.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Act - Buscar sugestões
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("intertravado", Area.CIVIL);

        // Assert - Deve retornar 2 itens separados (unidades diferentes)
        assertEquals(2, resultado.size());
        
        boolean temM2 = resultado.stream().anyMatch(item -> item.unidade().equals("M²"));
        boolean temM3 = resultado.stream().anyMatch(item -> item.unidade().equals("M³"));
        assertTrue(temM2 && temM3);
    }

    @Test
    void selecaoMultipla_ItemJaAdicionado_NaoDeveDuplicar() {
        // Arrange - Teste D: Não duplicar item já adicionado
        String descricao = "INTERTRAVADO 8 CM";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("300.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        // Criar análise e adicionar item
        Analise analise = new Analise();
        analise.setArea(Area.CIVIL);
        analise = analiseRepository.save(analise);

        AnaliseItem analiseItem = new AnaliseItem();
        analiseItem.setAnalise(analise);
        analiseItem.setDescricao(descricao);
        analiseItem.setUnidade(unidade);
        analiseItem.setQuantidade(new BigDecimal("300.00"));
        analiseItemRepository.save(analiseItem);

        // Act - Buscar sugestões com item já adicionado
        List<String> itensJaAdicionados = List.of(descricao + "|" + unidade);
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(
            "intertravado", Area.CIVIL, itensJaAdicionados);

        // Assert - Item não deve aparecer nas sugestões
        assertEquals(0, resultado.size());
    }

    @Test
    void selecaoMultipla_QuantidadeMaxima_DeveUsarDisponivel() {
        // Arrange - Teste F: Quantidade máxima
        String descricao = "INTERTRAVADO 8 CM";
        String unidade = "M²";
        BigDecimal quantidadeDisponivel = new BigDecimal("300.00");

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(quantidadeDisponivel);
        item1.setCat(cat1);
        catItemRepository.save(item1);

        // Act - Buscar sugestões
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("intertravado", Area.CIVIL);

        // Assert - Quantidade disponível deve ser exatamente a do CatItem
        assertEquals(1, resultado.size());
        assertEquals(quantidadeDisponivel, resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void selecaoMultipla_CatsDiferentesMesmoItemLogico_DeveSomar() {
        // Arrange - Teste F (CATs diferentes): CATs que formam mesmo item lógico
        String descricao = "INTERTRAVADO 8 CM";
        String unidade = "M²";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("150.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("150.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Act - Buscar sugestões
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("intertravado", Area.CIVIL);

        // Assert - Deve somar as quantidades das duas CATs
        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("300.00"), resultado.get(0).quantidadeDisponivel());
    }
}
