package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                ? catItemRepository.buscarDescricoesPorTermo(termo, area)
                : catItemRepository.buscarDescricoesPorTermo(termo);
        return resultados.stream()
                .map(row -> new ItemSugestaoDTO((String) row[0], (String) row[1]))
                .limit(20)
                .toList();
    }

    @GetMapping("/recentes")
    public List<ItemSugestaoDTO> buscarRecentes(@RequestParam(required = false) Area area) {
        List<Object[]> resultados = catItemRepository.buscarDescricoesRecentes(area);
        return resultados.stream()
                .map(row -> new ItemSugestaoDTO((String) row[0], (String) row[1]))
                .limit(10)
                .toList();
    }
}
