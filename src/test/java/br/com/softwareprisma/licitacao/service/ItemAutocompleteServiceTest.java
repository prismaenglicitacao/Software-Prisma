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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
