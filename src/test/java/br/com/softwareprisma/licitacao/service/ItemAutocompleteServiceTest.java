package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.enums.Area;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemAutocompleteServiceTest {

    @Mock
    private CatItemRepository catItemRepository;

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

    @BeforeEach
    void setUp() {
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
        
        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
                .thenReturn(List.of(catItem1, catItem2));
        when(descricaoMatcher.gerarChave(catItem1.getDescricao(), catItem1.getUnidade()))
                .thenReturn(chaveNormalizada);
        when(descricaoMatcher.gerarChave(catItem2.getDescricao(), catItem2.getUnidade()))
                .thenReturn(chaveNormalizada);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL);

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
        
        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
                .thenReturn(List.of(item1, item2));
        when(descricaoMatcher.gerarChave(item1.getDescricao(), item1.getUnidade()))
                .thenReturn(chave1);
        when(descricaoMatcher.gerarChave(item2.getDescricao(), item2.getUnidade()))
                .thenReturn(chave2);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL);

        // Assert
        assertEquals(2, resultado.size(), "Deve retornar duas sugestões separadas");
    }

    @Test
    void buscarSugestoesAgrupadas_TermoCurto_DeveRetornarListaVazia() {
        // Arrange
        String termo = "p";

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL);

        // Assert
        assertEquals(0, resultado.size());
    }

    @Test
    void buscarSugestoesAgrupadas_TermoNulo_DeveRetornarListaVazia() {
        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(null, Area.CIVIL);

        // Assert
        assertEquals(0, resultado.size());
    }

    @Test
    void buscarSugestoesAgrupadas_SemArea_DeveFuncionar() {
        // Arrange
        String termo = "passeio";
        String chaveNormalizada = "chave|M2";
        
        when(catItemRepository.buscarItensPorTermoParaAutocomplete(termo))
                .thenReturn(List.of(catItem1));
        when(descricaoMatcher.gerarChave(catItem1.getDescricao(), catItem1.getUnidade()))
                .thenReturn(chaveNormalizada);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, null);

        // Assert
        assertEquals(1, resultado.size());
    }

    @Test
    void buscarSugestoesAgrupadas_ComItensJaAdicionados_DeveFiltrar() {
        // Arrange
        String termo = "passeio";
        String chaveNormalizada = "passeio de concreto 1 3 5 com 5 0 cm de espessura e juntas riscadas em quadros de 1 0 x 2 0 m|M2";
        
        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
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
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, itensJaAdicionados);

        // Assert
        assertEquals(0, resultado.size(), "Deve filtrar itens com chave normalizada equivalente");
    }

    @Test
    void buscarSugestoesAgrupadas_ComItensJaAdicionados_Diferentes_DeveManter() {
        // Arrange
        String termo = "passeio";
        String chave1 = "passeio tipo a|M2";
        String chave2 = "passeio tipo b|M2";
        
        // Criar itens com descrições diferentes
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
        
        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
                .thenReturn(List.of(item1, item2));
        when(descricaoMatcher.gerarChave(item1.getDescricao(), item1.getUnidade()))
                .thenReturn(chave1);
        when(descricaoMatcher.gerarChave(item2.getDescricao(), item2.getUnidade()))
                .thenReturn(chave2);
        
        // Item já adicionado com chave diferente
        List<String> itensJaAdicionados = List.of("OUTRO ITEM|M²");
        when(descricaoMatcher.gerarChave("OUTRO ITEM", "M²"))
                .thenReturn("outro item|M2");

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL, itensJaAdicionados);

        // Assert
        assertEquals(2, resultado.size(), "Deve manter itens com chaves diferentes");
    }

    @Test
    void buscarSugestoesAgrupadas_MesmaDescricaoUnidadesDiferentes_DeveGerarOpcoesSeparadas() {
        // Arrange
        String termo = "varricao";
        String descricao = "VARRIÇÃO DE VIAS URBANAS PAVIMENTADAS";
        
        CatItem itemM = new CatItem();
        itemM.setId(1L);
        itemM.setDescricao(descricao);
        itemM.setUnidade("M");
        itemM.setQuantidade(new BigDecimal("100.00"));
        itemM.setCat(cat1);

        CatItem itemKM = new CatItem();
        itemKM.setId(2L);
        itemKM.setDescricao(descricao);
        itemKM.setUnidade("KM");
        itemKM.setQuantidade(new BigDecimal("200.00"));
        itemKM.setCat(cat2);

        CatItem itemKMSarjeta = new CatItem();
        itemKMSarjeta.setId(3L);
        itemKMSarjeta.setDescricao(descricao);
        itemKMSarjeta.setUnidade("KM/SARJETA");
        itemKMSarjeta.setQuantidade(new BigDecimal("300.00"));
        itemKMSarjeta.setCat(cat1);

        String chaveM = "varricao de vias urbanas pavimentadas|M";
        String chaveKM = "varricao de vias urbanas pavimentadas|KM";
        String chaveKMSarjeta = "varricao de vias urbanas pavimentadas|KM/SARJETA";

        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
                .thenReturn(List.of(itemM, itemKM, itemKMSarjeta));
        when(descricaoMatcher.gerarChave(descricao, "M")).thenReturn(chaveM);
        when(descricaoMatcher.gerarChave(descricao, "KM")).thenReturn(chaveKM);
        when(descricaoMatcher.gerarChave(descricao, "KM/SARJETA")).thenReturn(chaveKMSarjeta);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL);

        // Assert
        assertEquals(3, resultado.size(), "Deve retornar 3 sugestões separadas para unidades diferentes");
        
        // Verificar que cada unidade aparece separadamente
        boolean temM = resultado.stream().anyMatch(dto -> dto.unidade().equals("M"));
        boolean temKM = resultado.stream().anyMatch(dto -> dto.unidade().equals("KM"));
        boolean temKMSarjeta = resultado.stream().anyMatch(dto -> dto.unidade().equals("KM/SARJETA"));
        
        assertTrue(temM, "Deve conter unidade M");
        assertTrue(temKM, "Deve conter unidade KM");
        assertTrue(temKMSarjeta, "Deve conter unidade KM/SARJETA");
    }

    @Test
    void buscarSugestoesAgrupadas_MesmaDescricaoMesmaUnidade_DeveAgruparESomar() {
        // Arrange
        String termo = "varricao";
        String descricao = "VARRIÇÃO DE VIAS URBANAS PAVIMENTADAS";
        String unidade = "M";
        
        CatItem item1 = new CatItem();
        item1.setId(1L);
        item1.setDescricao(descricao);
        item1.setUnidade(unidade);
        item1.setQuantidade(new BigDecimal("100.00"));
        item1.setCat(cat1);

        CatItem item2 = new CatItem();
        item2.setId(2L);
        item2.setDescricao(descricao);
        item2.setUnidade(unidade);
        item2.setQuantidade(new BigDecimal("200.00"));
        item2.setCat(cat2);

        CatItem item3 = new CatItem();
        item3.setId(3L);
        item3.setDescricao(descricao);
        item3.setUnidade(unidade);
        item3.setQuantidade(new BigDecimal("150.00"));
        item3.setCat(cat1);

        String chave = "varricao de vias urbanas pavimentadas|M";

        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
                .thenReturn(List.of(item1, item2, item3));
        when(descricaoMatcher.gerarChave(descricao, unidade)).thenReturn(chave);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL);

        // Assert
        assertEquals(1, resultado.size(), "Deve retornar apenas uma sugestão agrupada");
        assertEquals(descricao, resultado.get(0).descricao());
        assertEquals(unidade, resultado.get(0).unidade());
        assertEquals(new BigDecimal("450.00"), resultado.get(0).quantidadeDisponivel(), 
                "Quantidade deve ser a soma: 100 + 200 + 150 = 450");
    }

    @Test
    void buscarSugestoesAgrupadas_VariasCATsUnidadesDiferentes_DeveAgruparPorUnidade() {
        // Arrange
        String termo = "item";
        String descricao = "ITEM X";
        
        CatItem itemM1 = new CatItem();
        itemM1.setId(1L);
        itemM1.setDescricao(descricao);
        itemM1.setUnidade("M");
        itemM1.setQuantidade(new BigDecimal("100.00"));
        itemM1.setCat(cat1);

        CatItem itemM2 = new CatItem();
        itemM2.setId(2L);
        itemM2.setDescricao(descricao);
        itemM2.setUnidade("M");
        itemM2.setQuantidade(new BigDecimal("200.00"));
        itemM2.setCat(cat2);

        CatItem itemKM1 = new CatItem();
        itemKM1.setId(3L);
        itemKM1.setDescricao(descricao);
        itemKM1.setUnidade("KM");
        itemKM1.setQuantidade(new BigDecimal("300.00"));
        itemKM1.setCat(cat1);

        CatItem itemKM2 = new CatItem();
        itemKM2.setId(4L);
        itemKM2.setDescricao(descricao);
        itemKM2.setUnidade("KM");
        itemKM2.setQuantidade(new BigDecimal("400.00"));
        itemKM2.setCat(cat2);

        CatItem itemKMSarjeta = new CatItem();
        itemKMSarjeta.setId(5L);
        itemKMSarjeta.setDescricao(descricao);
        itemKMSarjeta.setUnidade("KM/SARJETA");
        itemKMSarjeta.setQuantidade(new BigDecimal("500.00"));
        itemKMSarjeta.setCat(cat1);

        String chaveM = "item x|M";
        String chaveKM = "item x|KM";
        String chaveKMSarjeta = "item x|KM/SARJETA";

        when(catItemRepository.buscarItensPorTermoParaAutocomplete(eq(termo), any()))
                .thenReturn(List.of(itemM1, itemM2, itemKM1, itemKM2, itemKMSarjeta));
        when(descricaoMatcher.gerarChave(descricao, "M")).thenReturn(chaveM);
        when(descricaoMatcher.gerarChave(descricao, "KM")).thenReturn(chaveKM);
        when(descricaoMatcher.gerarChave(descricao, "KM/SARJETA")).thenReturn(chaveKMSarjeta);

        // Act
        List<ItemSugestaoDTO> resultado = itemAutocompleteService.buscarSugestoesAgrupadas(termo, Area.CIVIL);

        // Assert
        assertEquals(3, resultado.size(), "Deve retornar 3 sugestões agrupadas por unidade");
        
        // Verificar quantidades por unidade
        ItemSugestaoDTO itemM = resultado.stream().filter(dto -> dto.unidade().equals("M")).findFirst().orElse(null);
        ItemSugestaoDTO itemKM = resultado.stream().filter(dto -> dto.unidade().equals("KM")).findFirst().orElse(null);
        ItemSugestaoDTO itemKMSarjetaResult = resultado.stream().filter(dto -> dto.unidade().equals("KM/SARJETA")).findFirst().orElse(null);
        
        assertNotNull(itemM);
        assertNotNull(itemKM);
        assertNotNull(itemKMSarjetaResult);
        
        assertEquals(new BigDecimal("300.00"), itemM.quantidadeDisponivel(), "M deve somar 100 + 200");
        assertEquals(new BigDecimal("700.00"), itemKM.quantidadeDisponivel(), "KM deve somar 300 + 400");
        assertEquals(new BigDecimal("500.00"), itemKMSarjetaResult.quantidadeDisponivel(), "KM/SARJETA deve ser 500");
    }
}
