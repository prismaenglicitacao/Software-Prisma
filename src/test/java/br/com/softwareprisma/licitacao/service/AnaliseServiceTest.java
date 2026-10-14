package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.*;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AnaliseServiceTest {

    @Autowired
    private AnaliseService analiseService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EngenheiroRepository engenheiroRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private CatItemRepository catItemRepository;

    @Autowired
    private AnaliseRepository analiseRepository;

    @Autowired
    private br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher descricaoMatcher;

    @Autowired
    private br.com.softwareprisma.licitacao.service.matcher.ValorTecnicoExtractor valorTecnicoExtractor;

    @Autowired
    private br.com.softwareprisma.licitacao.service.matcher.AtributoQualitativoExtractor atributoQualitativoExtractor;

    @Test
    void testeA_DuasCATs_DoisEngenheiros_MesmaEmpresa_CasoProducaoTuboCorrugado() {
        // Criar empresa
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa Teste Prisma");
        empresa.setCnpj("12345678000100");
        empresa = empresaRepository.save(empresa);

        // Criar engenheiros
        Engenheiro erika = new Engenheiro();
        erika.setNome("ERIKA");
        erika.setArea(Area.CIVIL);
        erika.setEmpresa(empresa);
        erika = engenheiroRepository.save(erika);

        Engenheiro stive = new Engenheiro();
        stive.setNome("STIVE");
        stive.setArea(Area.CIVIL);
        stive.setEmpresa(empresa);
        stive = engenheiroRepository.save(stive);

        // Criar CATs
        Cat cat1 = new Cat();
        cat1.setNome("CAT 2220649609-2026 - OLINDA MICRO E MACRO.2");
        cat1.setNumeroCat("2220649609/2026");
        cat1.setMunicipio("Olinda");
        cat1.setEngenheiro(erika);
        cat1 = catRepository.save(cat1);

        Cat cat2 = new Cat();
        cat2.setNome("CAT 2220651144-2026 - MICRO E MACRO OLINDA II");
        cat2.setNumeroCat("2220651144/2026");
        cat2.setMunicipio("Olinda");
        cat2.setEngenheiro(stive);
        cat2 = catRepository.save(cat2);

        // Descrições reais do casode produção
        String descricaoA = "FORNECIMENTO E ASSENTAMENTO DE TUBO CORRUGADO EM PEAD MODELO N12-WT PARA DRENAGEM, COM DIÂMETRO DE 600 MM, TIGRE OU SIMILAR, INCLUINDO PASTA LUBRIFICANTE E TRANSPORTE DOS TUBOS COM CARREGO E DESCARREGO NA OBRA (POSTO OBRA GRANDE RECIFE)";
        String descricaoB = "FORNECIMENTO E ASSENTAMENTO DE TUBO CORRUGADO EM PEAD MODELO N12-WT PARA DRENAGEM, COM DIÂMETRO DE 600 MM, TIGRE OU SIMILAR, INCLUINDO PASTA LUBRIFICANTE E TRANSPORTE DOS TUBOS COM CARREGO E DESCARREGO NA OBRA. (POSTO OBRA GRANDE RECIFE).";

        // Criar CatItems e adicionar às CATs
        CatItem item1 = new CatItem();
        item1.setCat(cat1);
        item1.setDescricao(descricaoA);
        item1.setUnidade("M");
        item1.setQuantidade(new BigDecimal("24.00"));
        cat1.getItens().add(item1);
        item1 = catItemRepository.save(item1);

        CatItem item2 = new CatItem();
        item2.setCat(cat2);
        item2.setDescricao(descricaoB);
        item2.setUnidade("M");
        item2.setQuantidade(new BigDecimal("24.00"));
        cat2.getItens().add(item2);
        item2 = catItemRepository.save(item2);

        // Forçar recarregamento das CATs para verificar se os itens foram associados
        cat1 = catRepository.buscarDetalhadaPorId(cat1.getId()).orElseThrow();
        cat2 = catRepository.buscarDetalhadaPorId(cat2.getId()).orElseThrow();
        
        System.out.println("=== VERIFICAÇÃO DE PERSISTÊNCIA ===");
        System.out.println("CAT1 itens: " + cat1.getItens().size());
        System.out.println("CAT2 itens: " + cat2.getItens().size());

        // Criar análise
        Analise analise = new Analise();
        analise.setArea(Area.CIVIL);
        analise.setEmpresa(empresa);
        analise = analiseRepository.save(analise);

        // Criar item exigido
        AnaliseItem itemExigido = new AnaliseItem();
        itemExigido.setAnalise(analise);
        itemExigido.setDescricao(descricaoA);
        itemExigido.setUnidade("M");
        itemExigido.setQuantidade(new BigDecimal("48.00"));
        analise.getItens().add(itemExigido);
        analiseRepository.save(analise);

        // Executar análise usando o método público prepararAnalise
        AnaliseResultado resultado = analiseService.prepararAnalise(analise.getId());

        // Verificar resultado
        System.out.println("=== RESULTADO DA ANÁLISE ===");
        System.out.println("Resultado: " + resultado.resultado());
        System.out.println("Cobertura: " + resultado.cobertura());
        System.out.println("Engenheiros: " + resultado.engenheiros());
        System.out.println("CATs: " + resultado.cats());
        System.out.println("Número de itens no resultado: " + resultado.itens().size());
        
        // Verificar CatItems disponíveis
        List<Cat> cats = catRepository.listarPorEmpresa(empresa);
        System.out.println("=== CATS DISPONÍVEIS ===");
        for (Cat cat : cats) {
            System.out.println("CAT: " + cat.getNome() + " | Engenheiro: " + cat.getEngenheiro().getNome() + " | Área: " + cat.getEngenheiro().getArea());
            for (CatItem item : cat.getItens()) {
                System.out.println("  - CatItem ID: " + item.getId() + " | Descrição: " + item.getDescricao().substring(0, Math.min(50, item.getDescricao().length())) + "..." + " | Qtd: " + item.getQuantidade() + " " + item.getUnidade());
            }
        }
        
        // Verificar matching direto
        System.out.println("=== TESTE DE MATCHING DIRETO ===");
        for (Cat cat : cats) {
            for (CatItem item : cat.getItens()) {
                boolean corresponde = descricaoMatcher.corresponde(descricaoA, "M", item.getDescricao(), item.getUnidade());
                System.out.println("CatItem " + item.getId() + " corresponde ao requisito (DescricaoMatcher): " + corresponde);
                System.out.println("  Chave requisito: " + descricaoMatcher.gerarChave(descricaoA, "M"));
                System.out.println("  Chave item: " + descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade()));
                System.out.println("  Valores técnicos requisito: " + valorTecnicoExtractor.extrair(descricaoA));
                System.out.println("  Valores técnicos item: " + valorTecnicoExtractor.extrair(item.getDescricao()));
                System.out.println("  Atributos qualitativos requisito: " + atributoQualitativoExtractor.extrair(descricaoA));
                System.out.println("  Atributos qualitativos item: " + atributoQualitativoExtractor.extrair(item.getDescricao()));
                
                // Testar normalização conservadora manualmente
                String desc1Normalizada = java.text.Normalizer.normalize(descricaoA, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
                String desc2Normalizada = java.text.Normalizer.normalize(item.getDescricao(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
                System.out.println("  Descricao normalizada conservadora requisito: " + desc1Normalizada);
                System.out.println("  Descricao normalizada conservadora item: " + desc2Normalizada);
                System.out.println("  São iguais (normalização conservadora): " + desc1Normalizada.equals(desc2Normalizada));
            }
        }

        if (!resultado.itens().isEmpty()) {
            AnaliseResultadoItem itemResultado = resultado.itens().get(0);
            System.out.println("Item exigido: " + itemResultado.exigido());
            System.out.println("Item encontrado: " + itemResultado.encontrado());
            System.out.println("Item atende: " + itemResultado.atende());
            System.out.println("Origens: " + itemResultado.origens());
        }

        // Assert: Deve encontrar 48 M (24 + 24)
        assertEquals(1, resultado.itens().size(), "Deve ter 1 item no resultado");
        AnaliseResultadoItem itemResultado = resultado.itens().get(0);
        assertEquals(new BigDecimal("48.00"), itemResultado.encontrado(), 
            "Deve encontrar 48 M (soma dos dois CatItems de 24 M)");
        assertTrue(itemResultado.atende(), "Item deve atender");
        assertEquals(2, itemResultado.origens().size(), 
            "Deve ter 2 origens (duas CATs)");
    }
}
