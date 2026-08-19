package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.enums.Area;
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
}
