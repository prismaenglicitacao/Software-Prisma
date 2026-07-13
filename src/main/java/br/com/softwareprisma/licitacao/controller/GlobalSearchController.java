package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.GlobalSearchDTO;
import br.com.softwareprisma.licitacao.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping("/api/pesquisa")
    public GlobalSearchDTO pesquisar(@RequestParam String q) {
        return globalSearchService.pesquisar(q);
    }
}
