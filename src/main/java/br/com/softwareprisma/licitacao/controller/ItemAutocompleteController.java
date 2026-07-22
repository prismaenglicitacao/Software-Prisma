package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/itens")
@RequiredArgsConstructor
public class ItemAutocompleteController {

    private final CatItemRepository catItemRepository;

    @GetMapping("/sugestoes")
    public List<ItemSugestaoDTO> buscarSugestoes(@RequestParam String termo,
                                                 @RequestParam(required = false) Area area) {
        if (termo == null || termo.trim().length() < 2) {
            return List.of();
        }

        List<Object[]> resultados = area != null
                ? catItemRepository.buscarDescricoesPorTermoComQuantidade(termo, area)
                : catItemRepository.buscarDescricoesPorTermoComQuantidade(termo);
        return resultados.stream()
                .map(row -> new ItemSugestaoDTO(
                        (String) row[0],
                        (String) row[1],
                        row[2] != null ? ((Number) row[2]).toString() != null
                                ? new BigDecimal(row[2].toString()) : null : null))
                .limit(100)
                .toList();
    }

    @GetMapping("/recentes")
    public List<ItemSugestaoDTO> buscarRecentes(@RequestParam(required = false) Area area) {
        List<Object[]> resultados = catItemRepository.buscarDescricoesRecentesComQuantidade(area);
        return resultados.stream()
                .map(row -> new ItemSugestaoDTO(
                        (String) row[0],
                        (String) row[1],
                        row[2] != null ? new BigDecimal(row[2].toString()) : null))
                .limit(10)
                .toList();
    }
}
