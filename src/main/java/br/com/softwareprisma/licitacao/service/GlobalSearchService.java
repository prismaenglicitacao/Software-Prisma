package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.GlobalSearchDTO;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import br.com.softwareprisma.licitacao.repository.projection.CatSearchProjection;
import br.com.softwareprisma.licitacao.repository.projection.EngenheiroSearchProjection;
import br.com.softwareprisma.licitacao.repository.projection.ItemSearchProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final EngenheiroRepository engenheiroRepository;
    private final CatRepository catRepository;
    private final CatItemRepository catItemRepository;

    @Transactional(readOnly = true)
    public GlobalSearchDTO pesquisar(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return new GlobalSearchDTO(List.of(), List.of(), List.of(), false);
        }

        String termoNormalizado = normalizarTermo(termo);

        List<EngenheiroSearchProjection> engenheiros = engenheiroRepository.pesquisarPorNome(termoNormalizado);
        List<CatSearchProjection> cats = catRepository.pesquisarPorNomeOuNumero(termoNormalizado);
        List<ItemSearchProjection> itens = catItemRepository.pesquisarPorDescricao(termoNormalizado);

        boolean temMaisResultados = engenheiros.size() >= 5 || cats.size() >= 5 || itens.size() >= 10;

        return new GlobalSearchDTO(
                converterEngenheiros(engenheiros),
                converterCats(cats),
                converterItens(itens),
                temMaisResultados
        );
    }

    private String normalizarTermo(String termo) {
        return termo.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private List<GlobalSearchDTO.EngenheiroResultado> converterEngenheiros(List<EngenheiroSearchProjection> projections) {
        return projections.stream()
                .map(p -> new GlobalSearchDTO.EngenheiroResultado(
                        p.id(),
                        p.nome(),
                        p.area() != null ? p.area().name() : null,
                        p.totalCats()
                ))
                .toList();
    }

    private List<GlobalSearchDTO.CatResultado> converterCats(List<CatSearchProjection> projections) {
        return projections.stream()
                .map(p -> new GlobalSearchDTO.CatResultado(
                        p.id(),
                        p.nome(),
                        p.numero(),
                        p.engenheiroNome(),
                        p.totalItens()
                ))
                .toList();
    }

    private List<GlobalSearchDTO.ItemResultado> converterItens(List<ItemSearchProjection> projections) {
        return projections.stream()
                .map(p -> new GlobalSearchDTO.ItemResultado(
                        p.id(),
                        p.catId(),
                        p.descricao(),
                        p.unidade(),
                        p.catNome(),
                        p.catNumero(),
                        p.engenheiroNome(),
                        p.area() != null ? p.area().name() : null
                ))
                .toList();
    }
}
