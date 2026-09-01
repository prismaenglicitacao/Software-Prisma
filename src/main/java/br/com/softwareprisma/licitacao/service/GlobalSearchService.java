package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.GlobalSearchDTO;
import br.com.softwareprisma.licitacao.domain.Empresa;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final EngenheiroRepository engenheiroRepository;
    private final CatRepository catRepository;
    private final CatItemRepository catItemRepository;

    @Transactional(readOnly = true)
    public GlobalSearchDTO pesquisar(String termo, Empresa empresa) {
        if (termo == null || termo.trim().isEmpty()) {
            return new GlobalSearchDTO(List.of(), List.of(), List.of(), false);
        }

        String termoNormalizado = normalizarTermo(termo);

        List<EngenheiroSearchProjection> engenheiros = empresa != null
                ? engenheiroRepository.pesquisarPorNomeEEmpresa(termoNormalizado, empresa, PageRequest.of(0, 5))
                : engenheiroRepository.pesquisarPorNome(termoNormalizado, PageRequest.of(0, 5));

        List<CatSearchProjection> cats = empresa != null
                ? catRepository.pesquisarPorNomeOuNumeroEEmpresa(termoNormalizado, empresa, PageRequest.of(0, 5))
                : catRepository.pesquisarPorNomeOuNumero(termoNormalizado, PageRequest.of(0, 5));

        List<ItemSearchProjection> itens = empresa != null
                ? catItemRepository.pesquisarPorDescricaoEEmpresa(termoNormalizado, empresa, PageRequest.of(0, 10))
                : catItemRepository.pesquisarPorDescricao(termoNormalizado, PageRequest.of(0, 10));

        boolean temMaisResultados = engenheiros.size() >= 5 || cats.size() >= 5 || itens.size() >= 10;

        return new GlobalSearchDTO(
                converterEngenheiros(engenheiros),
                converterCats(cats),
                converterItens(itens),
                temMaisResultados);
    }

    /**
     * Pesquisa global — uso exclusivo de contextos administrativos.
     * Não utilizar em fluxos normais de usuário (retorna dados de todas as empresas).
     */
    @Transactional(readOnly = true)
    public GlobalSearchDTO pesquisarGlobalAdmin(String termo) {
        return pesquisar(termo, null);
    }

    private String normalizarTermo(String termo) {
        return termo.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private List<GlobalSearchDTO.EngenheiroResultado> converterEngenheiros(List<EngenheiroSearchProjection> projections) {
        return projections.stream()
                .map(p -> new GlobalSearchDTO.EngenheiroResultado(
                        p.id(), p.nome(),
                        p.area() != null ? p.area().name() : null,
                        p.totalCats()))
                .toList();
    }

    private List<GlobalSearchDTO.CatResultado> converterCats(List<CatSearchProjection> projections) {
        if (projections.isEmpty()) return List.of();

        List<Long> catIds = projections.stream().map(CatSearchProjection::id).toList();
        List<Object[]> contagens = catItemRepository.contarItensPorCatIds(catIds);
        Map<Long, Long> contagemPorCatId = contagens.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return projections.stream()
                .map(p -> new GlobalSearchDTO.CatResultado(
                        p.id(), p.nome(), p.engenheiroNome(),
                        contagemPorCatId.getOrDefault(p.id(), 0L).intValue()))
                .toList();
    }

    private List<GlobalSearchDTO.ItemResultado> converterItens(List<ItemSearchProjection> projections) {
        return projections.stream()
                .map(p -> new GlobalSearchDTO.ItemResultado(
                        p.id(), p.catId(), p.descricao(), p.unidade(),
                        p.catNome(), p.engenheiroNome(),
                        p.area() != null ? p.area().name() : null))
                .toList();
    }
}
