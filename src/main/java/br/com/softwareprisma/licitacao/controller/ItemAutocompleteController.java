package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.ItemAutocompleteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/itens")
@RequiredArgsConstructor
public class ItemAutocompleteController {

    private final ItemAutocompleteService itemAutocompleteService;
    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping("/sugestoes")
    public List<ItemSugestaoDTO> buscarSugestoes(@RequestParam String termo,
                                                 @RequestParam(required = false) Area area,
                                                 @RequestParam(required = false) List<String> itensJaAdicionados,
                                                 HttpSession session,
                                                 @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        return itemAutocompleteService.buscarSugestoesAgrupadas(termo, area, itensJaAdicionados, empresa);
    }

    @GetMapping("/recentes")
    public List<ItemSugestaoDTO> buscarRecentes(@RequestParam(required = false) Area area,
                                                HttpSession session,
                                                @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        return itemAutocompleteService.buscarItensRecentes(area, empresa);
    }
}
