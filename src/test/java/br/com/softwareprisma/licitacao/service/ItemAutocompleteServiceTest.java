package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.repository.AnaliseItemRepository;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ItemAutocompleteServiceTest {

    @Mock
    private CatItemRepository catItemRepository;

        @Mock
        private AnaliseItemRepository analiseItemRepository;

    @Mock
    private DescricaoMatcher descricaoMatcher;

    @InjectMocks
    private ItemAutocompleteService itemAutocompleteService;

    private CatItem catItem1;
    private CatItem catItem2;
    private Engenheiro engenheiro1;
    private Engenheiro engenheiro2;
    private Cat cat1;
    private Cat cat2;
        private Empresa empresa;

    @BeforeEach
    void setUp() {
                empresa = new Empresa();
                empresa.setId(1L);
                lenient().when(descricaoMatcher.contemTermoNormalizado(anyString(), anyString())).thenReturn(true);

        engenheiro1 = new Engenheiro();
        engenheiro1.setId(1L);
        engenheiro1.setNome("Engenheiro A");
        engenheiro1.setArea(Area.CIVIL);

        engenheiro2 = new Engenheiro();
        engenheiro2.setId(2L);
        engenheiro2.setNome("Engenheiro B");
        engenheiro2.setArea(Area.CIVIL);

        cat1 = new Cat();
        cat1.setId(1L);
        cat1.setNome("CAT 1");
        cat1.setEngenheiro(engenheiro1);

        cat2 = new Cat();
        cat2.setId(2L);
        cat2.setNome("CAT 2");
        cat2.setEngenheiro(engenheiro2);

        String descricao = "PASSEIO DE CONCRETO 1:3:5 COM 5,0 CM DE ESPESSURA E JUNTAS RISCADAS EM QUADROS DE 1,0 X 2,0 M";
        String unidade = "M²";

        catItem1 = new CatItem();
        catItem1.setId(1L);
        catItem1.setDescricao(descricao);
        catItem1.setUnidade(unidade);
        catItem1.setQuantidade(new BigDecimal("688.54"));
        catItem1.setCat(cat1);

        catItem2 = new CatItem();
        catItem2.setId(2L);
        catItem2.setDescricao(descricao);
        catItem2.setUnidade(unidade);
        catItem2.setQuantidade(new BigDecimal("42.86"));
        catItem2.setCat(cat2);
    }

    @Test
    void buscarSugestoesAgrupadas_DeveAgruparCatItemsComMesmaDescricaoEUnidade() {
        // Arrange
        String termo = "passeio";
        String chaveNormalizada = "passeio de concreto 1 3 5 com 5 0 cm de espessura e juntas riscadas em quadros de 1 0 x 2 0 m|M2";
        
        when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                eq(termo), eq(Area.CIVIL.name()), eq(empresa.getId())))
                .thenReturn(List.of(catItem1, catItem2));
        when(descricaoMatcher.gerarChave(catItem1.getDescricao(), catItem1.getUnidade()))
                .thenReturn(chaveNormalizada);
        when(descricaoMatcher.gerarChave(catItem2.getDescricao(), catItem2.getUnidade()))
                .thenReturn(chaveNormalizada);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, null, empresa);

        // Assert
        assertEquals(1, resultado.size(), "Deve retornar apenas uma sugestão agrupada");
        
        ItemSugestaoDTO sugestao = resultado.get(0);
        assertEquals(catItem1.getDescricao(), sugestao.descricao());
        assertEquals(catItem1.getUnidade(), sugestao.unidade());
        assertEquals(new BigDecimal("731.40"), sugestao.quantidadeDisponivel(), 
                "Quantidade deve ser a soma: 688.54 + 42.86 = 731.40");
    }

    @Test
    void buscarSugestoesAgrupadas_DeveManterSeparadosCatItemsComChavesDiferentes() {
        // Arrange
        String termo = "passeio";
        String chave1 = "chave1|M2";
        String chave2 = "chave2|M2";
        
        // Criar CatItems com descrições diferentes
        CatItem item1 = new CatItem();
        item1.setId(1L);
        item1.setDescricao("PASSEIO DE CONCRETO TIPO A");
        item1.setUnidade("M²");
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);

        CatItem item2 = new CatItem();
        item2.setId(2L);
        item2.setDescricao("PASSEIO DE CONCRETO TIPO B");
        item2.setUnidade("M²");
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat2);
        
        when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                eq(termo), eq(Area.CIVIL.name()), eq(empresa.getId())))
                .thenReturn(List.of(item1, item2));
        when(descricaoMatcher.gerarChave(item1.getDescricao(), item1.getUnidade()))
                .thenReturn(chave1);
        when(descricaoMatcher.gerarChave(item2.getDescricao(), item2.getUnidade()))
                .thenReturn(chave2);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, null, empresa);

        // Assert
        assertEquals(2, resultado.size(), "Deve retornar duas sugestões separadas");
    }

    @Test
    void buscarSugestoesAgrupadas_TermoCurto_DeveRetornarListaVazia() {
        // Arrange
        String termo = "p";

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, null, empresa);

        // Assert
        assertEquals(0, resultado.size());
    }

    @Test
    void buscarSugestoesAgrupadas_TermoNulo_DeveRetornarListaVazia() {
        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(null, Area.CIVIL, null, empresa);

        // Assert
        assertEquals(0, resultado.size());
    }

    @Test
    void buscarSugestoesAgrupadas_SemArea_DeveFuncionar() {
        // Arrange
        String termo = "passeio";
        String chaveNormalizada = "chave|M2";
        
        when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(termo, null, empresa.getId()))
                .thenReturn(List.of(catItem1));
        when(descricaoMatcher.gerarChave(catItem1.getDescricao(), catItem1.getUnidade()))
                .thenReturn(chaveNormalizada);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, null, null, empresa);

        // Assert
        assertEquals(1, resultado.size());
    }

    @Test
    void buscarSugestoesAgrupadas_ComItensJaAdicionados_DeveFiltrar() {
        // Arrange
        String termo = "passeio";
        String chaveNormalizada = "passeio de concreto 1 3 5 com 5 0 cm de espessura e juntas riscadas em quadros de 1 0 x 2 0 m|M2";
        
        when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                eq(termo), eq(Area.CIVIL.name()), eq(empresa.getId())))
                .thenReturn(List.of(catItem1, catItem2));
        when(descricaoMatcher.gerarChave(catItem1.getDescricao(), catItem1.getUnidade()))
                .thenReturn(chaveNormalizada);
        when(descricaoMatcher.gerarChave(catItem2.getDescricao(), catItem2.getUnidade()))
                .thenReturn(chaveNormalizada);
        
        // Item já adicionado com descrição equivalente (com ponto no final)
        List<String> itensJaAdicionados = List.of(
            "PASSEIO DE CONCRETO 1:3:5 COM 5,0 CM DE ESPESSURA E JUNTAS RISCADAS EM QUADROS DE 1,0 X 2,0 M.|M²"
        );
        when(descricaoMatcher.gerarChave(
            "PASSEIO DE CONCRETO 1:3:5 COM 5,0 CM DE ESPESSURA E JUNTAS RISCADAS EM QUADROS DE 1,0 X 2,0 M.", 
            "M²"
        )).thenReturn(chaveNormalizada);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, itensJaAdicionados, empresa);

        // Assert
        assertEquals(0, resultado.size(), "Deve filtrar itens com chave normalizada equivalente");
    }

    @Test
    void buscarSugestoesAgrupadas_ComItensJaAdicionados_Diferentes_DeveManter() {
        // Arrange
        String termo = "passeio";
        String chave1 = "passeio tipo a|M2";
        String chave2 = "passeio tipo b|M2";
        catItem1.setDescricao("PASSEIO TIPO A");
        catItem2.setDescricao("PASSEIO TIPO B");
        
        when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                eq(termo), eq(Area.CIVIL.name()), eq(empresa.getId())))
                .thenReturn(List.of(catItem1, catItem2));
        when(descricaoMatcher.gerarChave(catItem1.getDescricao(), catItem1.getUnidade()))
                .thenReturn(chave1);
        when(descricaoMatcher.gerarChave(catItem2.getDescricao(), catItem2.getUnidade()))
                .thenReturn(chave2);
        
        // Item já adicionado com chave diferente
        List<String> itensJaAdicionados = List.of("OUTRO ITEM|M²");
        when(descricaoMatcher.gerarChave("OUTRO ITEM", "M²"))
                .thenReturn("outro item|M2");

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, itensJaAdicionados, empresa);

        // Assert
        assertEquals(2, resultado.size(), "Deve manter itens com chaves diferentes");
    }

        @Test
        void buscarSugestoesAgrupadas_SemEmpresa_DeveRejeitarSemConsultarRepository() {
                ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                () -> itemAutocompleteService.buscarSugestoesAgrupadas("item", Area.CIVIL, null, null));

                assertEquals(403, exception.getStatusCode().value());
                verifyNoInteractions(catItemRepository, analiseItemRepository);
        }

        @Test
        void buscarSugestoesAgrupadas_DeveConsultarSomenteEmpresaInformada() {
                when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                                "item", Area.CIVIL.name(), empresa.getId()))
                                .thenReturn(List.of());

                itemAutocompleteService.buscarSugestoesAgrupadas("item", Area.CIVIL, null, empresa);

                verify(catItemRepository).buscarItensPorTermoParaAutocompleteEEmpresa(
                                "item", Area.CIVIL.name(), empresa.getId());
                verify(catItemRepository, never()).buscarItensPorTermoParaAutocomplete(anyString(), any());
                verify(catItemRepository, never()).buscarItensPorTermoParaAutocomplete(anyString());
        }

        @Test
        void buscarItensRecentes_SemEmpresa_DeveRejeitarSemConsultarRepositories() {
                ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                () -> itemAutocompleteService.buscarItensRecentes(Area.CIVIL, null));

                assertEquals(403, exception.getStatusCode().value());
                verifyNoInteractions(catItemRepository, analiseItemRepository);
        }

        @Test
        void buscarItensRecentes_DeveConsultarSomenteEmpresaInformada() {
                when(analiseItemRepository.buscarRecentesPorEmpresa(Area.CIVIL, empresa)).thenReturn(List.of());

                assertEquals(List.of(), itemAutocompleteService.buscarItensRecentes(Area.CIVIL, empresa));

                verify(analiseItemRepository).buscarRecentesPorEmpresa(Area.CIVIL, empresa);
                verify(analiseItemRepository, never()).buscarRecentes(any());
                verifyNoInteractions(catItemRepository);
        }

        @Test
        void buscarItensRecentes_DeveFiltrarAnalisesECatItemsPelaMesmaEmpresa() {
                AnaliseItem analiseItem = new AnaliseItem();
                analiseItem.setDescricao("Item recente");
                analiseItem.setUnidade("UND");
                when(analiseItemRepository.buscarRecentesPorEmpresa(Area.CIVIL, empresa))
                                .thenReturn(List.of(analiseItem));
                when(descricaoMatcher.gerarChave("Item recente", "UND")).thenReturn("item recente|UND");
                when(catItemRepository.buscarTodosPorAreaEEmpresa(Area.CIVIL, empresa)).thenReturn(List.of());

                assertEquals(List.of(), itemAutocompleteService.buscarItensRecentes(Area.CIVIL, empresa));

                verify(analiseItemRepository).buscarRecentesPorEmpresa(Area.CIVIL, empresa);
                verify(catItemRepository).buscarTodosPorAreaEEmpresa(Area.CIVIL, empresa);
                verify(analiseItemRepository, never()).buscarRecentes(any());
                verify(catItemRepository, never()).buscarTodosPorArea(any());
                verify(catItemRepository, never()).buscarTodos();
        }

        @Test
        void diagnosticarCasoTuboCorrugado_DuasDescricoesComPontuacaoDiferente() {
                String descricaoA = "FORNECIMENTO E ASSENTAMENTO DE TUBO CORRUGADO EM PEAD MODELO N12-WT PARA DRENAGEM, COM DIÂMETRO DE 600 MM, TIGRE OU SIMILAR, INCLUINDO PASTA LUBRIFICANTE E TRANSPORTE DOS TUBOS COM CARREGO E DESCARREGO NA OBRA (POSTO OBRA GRANDE RECIFE)";
                String descricaoB = "FORNECIMENTO E ASSENTAMENTO DE TUBO CORRUGADO EM PEAD MODELO N12-WT PARA DRENAGEM, COM DIÂMETRO DE 600 MM, TIGRE OU SIMILAR, INCLUINDO PASTA LUBRIFICANTE E TRANSPORTE DOS TUBOS COM CARREGO E DESCARREGO NA OBRA. (POSTO OBRA GRANDE RECIFE).";

                CatItem item1 = new CatItem();
                item1.setId(1L);
                item1.setDescricao(descricaoA);
                item1.setUnidade("M");
                item1.setQuantidade(new BigDecimal("24.00"));
                item1.setCat(cat1);

                CatItem item2 = new CatItem();
                item2.setId(2L);
                item2.setDescricao(descricaoB);
                item2.setUnidade("M");
                item2.setQuantidade(new BigDecimal("24.00"));
                item2.setCat(cat2);

                String chaveEsperada = "fornecimento e assentamento de tubo corrugado em pead modelo n12 wt para drenagem com diametro de600mm tigre ou similar incluindo pasta lubrificante e transporte dos tubos com carrego e descarrego na obra posto obra grande recife|m";

                when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                                eq("tubo"), eq(Area.CIVIL.name()), eq(empresa.getId())))
                        .thenReturn(List.of(item1, item2));
                when(descricaoMatcher.gerarChave(descricaoA, "M")).thenReturn(chaveEsperada);
                when(descricaoMatcher.gerarChave(descricaoB, "M")).thenReturn(chaveEsperada);

                List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("tubo", Area.CIVIL, null, empresa);

                System.out.println("=== DIAGNÓSTICO CASO TUBO CORRUGADO (AUTOCOMPLETE) ===");
                System.out.println("Número de sugestões: " + resultado.size());
                if (!resultado.isEmpty()) {
                        ItemSugestaoDTO sugestao = resultado.get(0);
                        System.out.println("Descrição: " + sugestao.descricao().substring(0, Math.min(80, sugestao.descricao().length())) + "...");
                        System.out.println("Unidade: " + sugestao.unidade());
                        System.out.println("Quantidade: " + sugestao.quantidadeDisponivel());
                        System.out.println("Esperado: 1 sugestão com 48.00 M");
                }

                assertEquals(1, resultado.size(), "Deve agrupar as duas descrições em uma sugestão");
                assertEquals(new BigDecimal("48.00"), resultado.get(0).quantidadeDisponivel());
        }

        @Test
        void diagnosticarCasoPEAD_0_60_M_DuasDescricoesComAcentoEEspaco() {
                String descricaoA = "ASSENTAMENTO DE TUBOS PEAD 0,60M DE DIAMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE.";
                String descricaoB = "ASSENTAMENTO DE TUBOS PEAD 0,60 M DE DIÂMETRO, INCLUSIVE O FORNECIMENTO DOS MESMOS E TRANSPORTE";

                CatItem item1 = new CatItem();
                item1.setId(1L);
                item1.setDescricao(descricaoA);
                item1.setUnidade("M");
                item1.setQuantidade(new BigDecimal("50.00"));
                item1.setCat(cat1);

                CatItem item2 = new CatItem();
                item2.setId(2L);
                item2.setDescricao(descricaoB);
                item2.setUnidade("M");
                item2.setQuantidade(new BigDecimal("68.00"));
                item2.setCat(cat2);

                String chaveEsperada = "assentamento de tubos pead0 60m de diametro inclusive o fornecimento dos mesmos e transporte|m";

                when(catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(
                                eq("pead"), eq(Area.CIVIL.name()), eq(empresa.getId())))
                        .thenReturn(List.of(item1, item2));
                when(descricaoMatcher.gerarChave(descricaoA, "M")).thenReturn(chaveEsperada);
                when(descricaoMatcher.gerarChave(descricaoB, "M")).thenReturn(chaveEsperada);

                List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas("pead", Area.CIVIL, null, empresa);

                System.out.println("=== DIAGNÓSTICO CASO PEAD 0,60 M (AUTOCOMPLETE) ===");
                System.out.println("Número de sugestões: " + resultado.size());
                if (!resultado.isEmpty()) {
                        ItemSugestaoDTO sugestao = resultado.get(0);
                        System.out.println("Descrição: " + sugestao.descricao());
                        System.out.println("Unidade: " + sugestao.unidade());
                        System.out.println("Quantidade: " + sugestao.quantidadeDisponivel());
                        System.out.println("Esperado: 1 sugestão com 118.00 M");
                }

                assertEquals(1, resultado.size(), "Deve agrupar as duas descrições em uma sugestão");
                assertEquals(new BigDecimal("118.00"), resultado.get(0).quantidadeDisponivel());
        }
}
