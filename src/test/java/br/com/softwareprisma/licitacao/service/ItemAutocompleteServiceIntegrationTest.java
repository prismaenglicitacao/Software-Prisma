package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseItemRepository;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
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
    private Empresa empresa;
    private Cat cat1;
    private Cat cat2;

    @Autowired
    private EmpresaRepository empresaRepository;

    private Analise criarAnalise() {
        Analise analise = new Analise();
        analise.setEmpresa(empresa);
        return analise;
    }

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setNome("Empresa de teste autocomplete");
        empresa = empresaRepository.save(empresa);

        engenheiro1 = new Engenheiro();
        engenheiro1.setNome("Engenheiro A");
        engenheiro1.setArea(Area.CIVIL);
        engenheiro1.setEmpresa(empresa);
        engenheiro1 = engenheiroRepository.save(engenheiro1);

        engenheiro2 = new Engenheiro();
        engenheiro2.setNome("Engenheiro B");
        engenheiro2.setArea(Area.CIVIL);
        engenheiro2.setEmpresa(empresa);
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL, null, empresa);

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
        List<ItemSugestaoDTO> resultadoAntes = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL, null, empresa);
        assertEquals(2, resultadoAntes.size(), "Antes da edicao devem aparecer separados");

        // Act 2: Editar item2 para ter mesma descricao que item1
        item2.setDescricao(descricao1);
        catItemRepository.save(item2);

        // Act 3: Apos edicao - devem aparecer agrupados
        List<ItemSugestaoDTO> resultadoDepois = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL, null, empresa);
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL, null, empresa);

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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", null, null, empresa);

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
        Analise analise = criarAnalise();
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", null, null, empresa);

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
            Analise analise = criarAnalise();
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL, empresa);

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
        Analise analise = criarAnalise();
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL, empresa);

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
        Analise analise1 = criarAnalise();
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

        Analise analise2 = criarAnalise();
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarItensRecentes(Area.CIVIL, empresa);

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
        Analise analise = criarAnalise();
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
        List<ItemSugestaoDTO> resultadoAutocomplete = itemAutocompleteService.buscarSugestoesAgrupadas("passeio", Area.CIVIL, null, empresa);
        List<ItemSugestaoDTO> resultadoRecentes = itemAutocompleteService.buscarItensRecentes(Area.CIVIL, empresa);

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
        Analise analiseAnterior = criarAnalise();
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
        Analise analise = criarAnalise();
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("intertravado", Area.CIVIL, null, empresa);

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
        Analise analise = criarAnalise();
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
            "intertravado", Area.CIVIL, itensJaAdicionados, empresa);

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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("intertravado", Area.CIVIL, null, empresa);

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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("intertravado", Area.CIVIL, null, empresa);

        // Assert - Deve somar as quantidades das duas CATs
        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("300.00"), resultado.get(0).quantidadeDisponivel());
    }

    // ============================================================
    // TESTES DE REGRESSÃO - CASOS DE PRODUÇÃO
    // ============================================================

    @Test
    void regressao_CasoA_TuboCorrugado_DoisItens24M_Cada() {
        // Arrange - Caso A: Tubo corrugado (produção: CatItems 1134 e 2533)
        String descricaoA = "FORNECIMENTO E ASSENTAMENTO DE TUBO CORRUGADO EM PEAD MODELO N12-WT PARA DRENAGEM, COM DIÂMETRO DE 600 MM, TIGRE OU SIMILAR, INCLUINDO PASTA LUBRIFICANTE E TRANSPORTE DOS TUBOS COM CARREGO E DESCARREGO NA OBRA (POSTO OBRA GRANDE RECIFE)";
        String descricaoB = "FORNECIMENTO E ASSENTAMENTO DE TUBO CORRUGADO EM PEAD MODELO N12-WT PARA DRENAGEM, COM DIÂMETRO DE 600 MM, TIGRE OU SIMILAR, INCLUINDO PASTA LUBRIFICANTE E TRANSPORTE DOS TUBOS COM CARREGO E DESCARREGO NA OBRA. (POSTO OBRA GRANDE RECIFE).";
        String unidade = "M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricaoA);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("24.00"));
        item1.setCat(cat1);
        item1 = catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricaoB);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("24.00"));
        item2.setCat(cat2);
        item2 = catItemRepository.save(item2);

        System.out.println("=== TESTE REGRESSÃO - CASO A: TUBO CORRUGADO ===");
        System.out.println("Item 1 ID: " + item1.getId());
        System.out.println("Item 1 Descrição: " + descricaoA.substring(0, Math.min(80, descricaoA.length())) + "...");
        System.out.println("Item 2 ID: " + item2.getId());
        System.out.println("Item 2 Descrição: " + descricaoB.substring(0, Math.min(80, descricaoB.length())) + "...");

        String chaveA = descricaoMatcher.gerarChave(descricaoA, unidade);
        String chaveB = descricaoMatcher.gerarChave(descricaoB, unidade);
        System.out.println("Chave A: " + chaveA);
        System.out.println("Chave B: " + chaveB);
        System.out.println("Chaves são iguais: " + chaveA.equals(chaveB));

        // Test 1: Query do repository
        List<CatItem> itensQuery = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "tubo corrugado em pead", Area.CIVIL.name(), empresa.getId());
        System.out.println("Itens retornados pela query: " + itensQuery.size());
        for (CatItem item : itensQuery) {
            System.out.println("  - ID: " + item.getId() + ", Descrição: " + item.getDescricao().substring(0, Math.min(50, item.getDescricao().length())) + "...");
        }

        // Test 2: Agrupamento do service
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("tubo corrugado em pead", Area.CIVIL, null, empresa);
        System.out.println("Sugestões do autocomplete: " + resultado.size());
        for (ItemSugestaoDTO sugestao : resultado) {
            System.out.println("  - Descrição: " + sugestao.descricao().substring(0, Math.min(50, sugestao.descricao().length())) + "...");
            System.out.println("  - Unidade: " + sugestao.unidade());
            System.out.println("  - Quantidade: " + sugestao.quantidadeDisponivel());
        }

        // Assert
        assertEquals(2, itensQuery.size(), "A query deve retornar ambos os itens");
        assertEquals(1, resultado.size(), "O autocomplete deve agrupar em uma sugestão");
        assertEquals(new BigDecimal("48.00"), resultado.get(0).quantidadeDisponivel(), "A quantidade deve ser 48.00 M");
    }

    @Test
    void regressao_CasoB_Pead060M_DoisItens997E118() {
        // Arrange - Caso B: PEAD 0,60 M (produção: CatItems 783 e 1071)
        String descricaoA = "ASSENTAMENTO DE TUBOS PEAD 0,60M DE DIAMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE.";
        String descricaoB = "ASSENTAMENTO DE TUBOS PEAD 0,60 M DE DIÂMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE";
        String unidade = "M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricaoA);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("997.00"));
        item1.setCat(cat1);
        item1 = catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricaoB);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("118.00"));
        item2.setCat(cat2);
        item2 = catItemRepository.save(item2);

        System.out.println("=== TESTE REGRESSÃO - CASO B: PEAD 0,60 M ===");
        System.out.println("Item 1 ID: " + item1.getId());
        System.out.println("Item 1 Descrição: " + descricaoA);
        System.out.println("Item 2 ID: " + item2.getId());
        System.out.println("Item 2 Descrição: " + descricaoB);

        String chaveA = descricaoMatcher.gerarChave(descricaoA, unidade);
        String chaveB = descricaoMatcher.gerarChave(descricaoB, unidade);
        System.out.println("Chave A: " + chaveA);
        System.out.println("Chave B: " + chaveB);
        System.out.println("Chaves são iguais: " + chaveA.equals(chaveB));

        // Test 1: The normalized query must bridge the missing space before M.
        List<CatItem> itensQuery1 = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "pead 0,60m", Area.CIVIL.name(), empresa.getId());
        System.out.println("Itens retornados pela query (pead 0,60m): " + itensQuery1.size());
        for (CatItem item : itensQuery1) {
            System.out.println("  - ID: " + item.getId() + ", Descrição: " + item.getDescricao());
        }

        // Test 2: Query do repository com termo "pead"
        List<CatItem> itensQuery2 = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "pead", Area.CIVIL.name(), empresa.getId());
        System.out.println("Itens retornados pela query (pead): " + itensQuery2.size());
        for (CatItem item : itensQuery2) {
            System.out.println("  - ID: " + item.getId() + ", Descrição: " + item.getDescricao());
        }

        // Test 3: Agrupamento do service
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("pead 0,60m", Area.CIVIL, null, empresa);
        System.out.println("Sugestões do autocomplete (pead 0,60m): " + resultado.size());
        for (ItemSugestaoDTO sugestao : resultado) {
            System.out.println("  - Descrição: " + sugestao.descricao());
            System.out.println("  - Unidade: " + sugestao.unidade());
            System.out.println("  - Quantidade: " + sugestao.quantidadeDisponivel());
        }

        // Assert
        assertEquals(2, itensQuery1.size(), "A query com 'pead 0,60m' deve retornar ambas as descrições");
        assertEquals(2, itensQuery2.size(), "A query com 'pead' deve retornar ambos os itens");
        assertEquals(1, resultado.size(), "O autocomplete deve agrupar em uma sugestão");
        assertEquals(new BigDecimal("1115.00"), resultado.get(0).quantidadeDisponivel(), "A quantidade deve ser 1115.00 M");
    }

    @Test
    void regressao_CasoB_Pead060M_TermosDiferentes() {
        // Arrange - Caso B: Testar diferentes termos de busca
        String descricaoA = "ASSENTAMENTO DE TUBOS PEAD 0,60M DE DIAMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE.";
        String descricaoB = "ASSENTAMENTO DE TUBOS PEAD 0,60 M DE DIÂMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE";
        String unidade = "M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricaoA);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("997.00"));
        item1.setCat(cat1);
        item1 = catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricaoB);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("118.00"));
        item2.setCat(cat2);
        item2 = catItemRepository.save(item2);

        System.out.println("=== TESTE REGRESSÃO - CASO B: TERMOS DIFERENTES ===");

        // Test 1: Termo "0,60"
        List<CatItem> query1 = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "0,60", Area.CIVIL.name(), empresa.getId());
        System.out.println("Query '0,60': " + query1.size() + " itens");

        // Test 2: Termo "0 60"
        List<CatItem> query2 = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "0 60", Area.CIVIL.name(), empresa.getId());
        System.out.println("Query '0 60': " + query2.size() + " itens");

        // Test 3: Termo "060"
        List<CatItem> query3 = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "060", Area.CIVIL.name(), empresa.getId());
        System.out.println("Query '060': " + query3.size() + " itens");

        // Test 4: Termo "tubos pead"
        List<CatItem> query4 = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                "tubos pead", Area.CIVIL.name(), empresa.getId());
        System.out.println("Query 'tubos pead': " + query4.size() + " itens");
    }

    @Test
    void regressao_CasoB_Pead060M_ComItemJaAdicionado() {
        // Arrange - Caso B: Testar filtro de itens já adicionados
        String descricaoA = "ASSENTAMENTO DE TUBOS PEAD 0,60M DE DIAMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE.";
        String descricaoB = "ASSENTAMENTO DE TUBOS PEAD 0,60 M DE DIÂMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE";
        String unidade = "M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricaoA);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("997.00"));
        item1.setCat(cat1);
        item1 = catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricaoB);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("118.00"));
        item2.setCat(cat2);
        item2 = catItemRepository.save(item2);

        System.out.println("=== TESTE REGRESSÃO - CASO B: ITEM JÁ ADICIONADO ===");

        // Test 1: Sem itens já adicionados
        List<ItemSugestaoDTO> resultado1 = itemAutocompleteService.buscarSugestoesAgrupadas("pead", Area.CIVIL, null, empresa);
        System.out.println("Sem itens já adicionados: " + resultado1.size() + " sugestões");
        if (!resultado1.isEmpty()) {
            System.out.println("  Quantidade: " + resultado1.get(0).quantidadeDisponivel());
        }

        // Test 2: Com item já adicionado (usando descrição normalizada)
        List<String> itensJaAdicionados = List.of(descricaoA + "|" + unidade);
        List<ItemSugestaoDTO> resultado2 = itemAutocompleteService.buscarSugestoesAgrupadas("pead", Area.CIVIL, itensJaAdicionados, empresa);
        System.out.println("Com item já adicionado: " + resultado2.size() + " sugestões");

        // Assert - Quando um item já adicionado, o grupo inteiro deve ser filtrado
        assertEquals(1, resultado1.size(), "Sem filtro deve retornar 1 sugestão");
        assertEquals(new BigDecimal("1115.00"), resultado1.get(0).quantidadeDisponivel());
        assertEquals(0, resultado2.size(), "Com item já adicionado deve retornar 0 sugestões");
    }

    // ============================================================
    // TESTES ADICIONAIS PARA A CORREÇÃO
    // ============================================================

    @Test
    void correcao_BuscaComAcento_DeveEncontrarSemAcento() {
        // Arrange - Descrição com acento
        String descricaoComAcento = "DIÂMETRO";
        String unidade = "M";

        CatItem item = new CatItem();
        item.setDescricao("TUBO DE " + descricaoComAcento + " 600 MM");
        item.setUnidade(unidade);
        item.setQuantidade(new BigDecimal("100.00"));
        item.setCat(cat1);
        catItemRepository.save(item);

        // Act - Buscar com termo sem acento
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("diametro", Area.CIVIL, null, empresa);

        // Assert
        assertEquals(1, resultado.size(), "Deve encontrar item mesmo buscando sem acento");
        assertEquals(new BigDecimal("100.00"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void correcao_BuscaComEspaco_DeveEncontrarSemEspaco() {
        // Arrange - Descrição com espaço entre número e letra
        String descricaoComEspaco = "0,60 M";
        String descricaoSemEspaco = "0,60M";

        CatItem item1 = new CatItem();
        item1.setDescricao("TUBO PEAD " + descricaoComEspaco);
        item1.setUnidade("M");
        item1.setQuantidade(new BigDecimal("50.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao("TUBO PEAD " + descricaoSemEspaco);
        item2.setUnidade("M");
        item2.setQuantidade(new BigDecimal("50.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Act - Buscar com termo com espaço
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("0,60 m", Area.CIVIL, null, empresa);

        // Assert - Deve agrupar ambos
        assertEquals(1, resultado.size(), "Deve agrupar itens com e sem espaço");
        assertEquals(new BigDecimal("100.00"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void correcao_BuscaComUnidadeTecnicaSuperscrita_DeveEncontrarAmbasRepresentacoes() {
        CatItem itemComSuperscrito = new CatItem();
        itemComSuperscrito.setDescricao("PISO TÉCNICO 50 M²");
        itemComSuperscrito.setUnidade("M²");
        itemComSuperscrito.setQuantidade(new BigDecimal("25.00"));
        itemComSuperscrito.setCat(cat1);
        catItemRepository.save(itemComSuperscrito);

        CatItem itemComNumero = new CatItem();
        itemComNumero.setDescricao("PISO TECNICO 50M2");
        itemComNumero.setUnidade("M²");
        itemComNumero.setQuantidade(new BigDecimal("75.00"));
        itemComNumero.setCat(cat2);
        catItemRepository.save(itemComNumero);

        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(
                "50 M2", Area.CIVIL, null, empresa);

        assertEquals(1, resultado.size(), "M² e M2 devem ser recuperados e agrupados");
        assertEquals(new BigDecimal("100.00"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void correcao_BuscaUnicodeNfd_DeveEncontrarDescricaoComEnhe() {
        CatItem item = new CatItem();
        item.setDescricao("TUBO CAÑON DE AÇO");
        item.setUnidade("M");
        item.setQuantidade(new BigDecimal("12.00"));
        item.setCat(cat1);
        catItemRepository.save(item);

        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(
                "canon", Area.CIVIL, null, empresa);

        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("12.00"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void correcao_SeparadoresEntrePalavras_NaoDeveRetornarFalsoCandidato() {
        CatItem descricaoComSeparador = new CatItem();
        descricaoComSeparador.setDescricao("SERVIÇO DE FOOBAR");
        descricaoComSeparador.setUnidade("M");
        descricaoComSeparador.setQuantidade(new BigDecimal("10.00"));
        descricaoComSeparador.setCat(cat1);
        catItemRepository.save(descricaoComSeparador);

        CatItem descricaoComDuasPalavras = new CatItem();
        descricaoComDuasPalavras.setDescricao("SERVIÇO DE FOO BAR");
        descricaoComDuasPalavras.setUnidade("M");
        descricaoComDuasPalavras.setQuantidade(new BigDecimal("20.00"));
        descricaoComDuasPalavras.setCat(cat2);
        catItemRepository.save(descricaoComDuasPalavras);

        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(
                "foo bar", Area.CIVIL, null, empresa);

        assertEquals(1, resultado.size());
        assertEquals("SERVIÇO DE FOO BAR", resultado.get(0).descricao());
        assertEquals(new BigDecimal("20.00"), resultado.get(0).quantidadeDisponivel());
    }

    @Test
    void correcao_UnidadesDiferentes_NaoDevemSerAgrupadas() {
        // Arrange - Mesma descrição, unidades diferentes
        String descricao = "TUBO PEAD 0,60 M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade("M");
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade("UN");
        item2.setQuantidade(new BigDecimal("50.00"));
        item2.setCat(cat2);
        catItemRepository.save(item2);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", Area.CIVIL, null, empresa);

        // Assert - Não devem ser agrupados
        assertEquals(2, resultado.size(), "Unidades diferentes não devem ser agrupadas");
    }

    @Test
    void correcao_EmpresasDiferentes_NaoDevemSerMisturadas() {
        // Arrange - Criar segunda empresa
        Empresa empresa2 = new Empresa();
        empresa2.setNome("Empresa 2");
        empresa2 = empresaRepository.save(empresa2);

        Engenheiro engenheiro3 = new Engenheiro();
        engenheiro3.setNome("Engenheiro C");
        engenheiro3.setArea(Area.CIVIL);
        engenheiro3.setEmpresa(empresa2);
        engenheiro3 = engenheiroRepository.save(engenheiro3);

        Cat cat3 = new Cat();
        cat3.setEngenheiro(engenheiro3);
        cat3.setNome("CAT 3");
        cat3.setNumeroCat("CAT-003");
        cat3.setMunicipio("Recife");
        cat3 = catRepository.save(cat3);

        // Mesma descrição em empresas diferentes
        String descricao = "TUBO PEAD 0,60 M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade("M");
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade("M");
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat3);
        catItemRepository.save(item2);

        // Act - Buscar na empresa 1
        List<ItemSugestaoDTO> resultado1 = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", Area.CIVIL, null, empresa);

        // Act - Buscar na empresa 2
        List<ItemSugestaoDTO> resultado2 = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", Area.CIVIL, null, empresa2);

        // Assert - Empresas diferentes devem permanecer isoladas
        assertEquals(1, resultado1.size(), "Empresa 1 deve ter 1 sugestão");
        assertEquals(new BigDecimal("100.00"), resultado1.get(0).quantidadeDisponivel());
        assertEquals(1, resultado2.size(), "Empresa 2 deve ter 1 sugestão");
        assertEquals(new BigDecimal("200.00"), resultado2.get(0).quantidadeDisponivel());
    }

    @Test
    void correcao_FiltroArea_DeveFuncionar() {
        // Arrange - Criar engenheiro de área diferente
        Engenheiro engenheiro3 = new Engenheiro();
        engenheiro3.setNome("Engenheiro C");
        engenheiro3.setArea(Area.ELETRICA);
        engenheiro3.setEmpresa(empresa);
        engenheiro3 = engenheiroRepository.save(engenheiro3);

        Cat cat3 = new Cat();
        cat3.setEngenheiro(engenheiro3);
        cat3.setNome("CAT 3");
        cat3.setNumeroCat("CAT-003");
        cat3.setMunicipio("Recife");
        cat3 = catRepository.save(cat3);

        String descricao = "TUBO PEAD 0,60 M";

        CatItem item1 = new CatItem();
        item1.setDescricao(descricao);
        item1.setUnidade("M");
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);
        catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setDescricao(descricao);
        item2.setUnidade("M");
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat3);
        catItemRepository.save(item2);

        // Act - Buscar na área CIVIL
        List<ItemSugestaoDTO> resultadoCivil = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", Area.CIVIL, null, empresa);

        // Act - Buscar na área ELETRICA
        List<ItemSugestaoDTO> resultadoEletrica = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", Area.ELETRICA, null, empresa);

        // Act - Buscar sem filtro de área
        List<ItemSugestaoDTO> resultadoTodas = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", null, null, empresa);

        // Assert
        assertEquals(1, resultadoCivil.size(), "Área CIVIL deve ter 1 sugestão");
        assertEquals(new BigDecimal("100.00"), resultadoCivil.get(0).quantidadeDisponivel());
        assertEquals(1, resultadoEletrica.size(), "Área ELETRICA deve ter 1 sugestão");
        assertEquals(new BigDecimal("200.00"), resultadoEletrica.get(0).quantidadeDisponivel());
        assertEquals(1, resultadoTodas.size(), "Sem filtro de área deve agrupar a descrição equivalente");
        assertEquals(new BigDecimal("300.00"), resultadoTodas.get(0).quantidadeDisponivel(),
                "Sem filtro de área deve somar os itens das duas áreas");
    }

    @Test
    void correcao_Limite100Resultados_DeveFuncionar() {
        // Arrange - Criar 105 itens diferentes
        for (int i = 0; i < 105; i++) {
            CatItem item = new CatItem();
            item.setDescricao("ITEM DE TESTE " + i);
            item.setUnidade("M");
            item.setQuantidade(new BigDecimal("10.00"));
            item.setCat(cat1);
            catItemRepository.save(item);
        }

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("item", Area.CIVIL, null, empresa);

        // Assert
        assertEquals(100, resultado.size(), "Deve limitar a 100 resultados");
    }
}
