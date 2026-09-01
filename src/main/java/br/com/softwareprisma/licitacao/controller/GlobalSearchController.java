package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.GlobalSearchDTO;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.GlobalSearchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;
    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping("/api/pesquisa")
    public GlobalSearchDTO pesquisar(@RequestParam String q,
                                     HttpSession session,
                                     @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        return globalSearchService.pesquisar(q, empresa);
    }
}
