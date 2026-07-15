package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.GlobalSearchDTO;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import br.com.softwareprisma.licitacao.repository.projection.CatSearchProjection;
import br.com.softwareprisma.licitacao.repository.projection.EngenheiroSearchProjection;
import br.com.softwareprisma.licitacao.repository.projection.ItemSearchProjection;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

                List<EngenheiroSearchProjection> engenheiros = engenheiroRepository.pesquisarPorNome(
                                termoNormalizado,
                                PageRequest.of(0, 5));

                List<CatSearchProjection> cats = catRepository.pesquisarPorNomeOuNumero(
                                termoNormalizado,
                                PageRequest.of(0, 5));

                List<ItemSearchProjection> itens = catItemRepository.pesquisarPorDescricao(
                                termoNormalizado,
                                PageRequest.of(0, 10));

                boolean temMaisResultados = engenheiros.size() >= 5 || cats.size() >= 5 || itens.size() >= 10;

                return new GlobalSearchDTO(
                                converterEngenheiros(engenheiros),
                                converterCats(cats),
                                converterItens(itens),
                                temMaisResultados);
        }

        private String normalizarTermo(String termo) {
                return termo.trim()
                                .replaceAll("\\s+", " ")
                                .toLowerCase();
        }

        private List<GlobalSearchDTO.EngenheiroResultado> converterEngenheiros(
                        List<EngenheiroSearchProjection> projections) {
                return projections.stream()
                                .map(p -> new GlobalSearchDTO.EngenheiroResultado(
                                                p.id(),
                                                p.nome(),
                                                p.area() != null ? p.area().name() : null,
                                                p.totalCats()))
                                .toList();
        }

        private List<GlobalSearchDTO.CatResultado> converterCats(List<CatSearchProjection> projections) {
                if (projections.isEmpty()) {
                        return List.of();
                }

                List<Long> catIds = projections.stream()
                                .map(CatSearchProjection::id)
                                .toList();

                List<Object[]> contagens = catItemRepository.contarItensPorCatIds(catIds);

                Map<Long, Long> contagemPorCatId = contagens.stream()
                                .collect(java.util.stream.Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]
                                ));

                return projections.stream()
                                .map(p -> new GlobalSearchDTO.CatResultado(
                                                p.id(),
                                                p.nome(),
                                                p.engenheiroNome(),
                                                contagemPorCatId.getOrDefault(p.id(), 0L).intValue()))
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
                                                p.engenheiroNome(),
                                                p.area() != null ? p.area().name() : null))
                                .toList();
        }
}
