package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empresa-ativa")
@RequiredArgsConstructor
public class EmpresaAtivaController {

    private final EmpresaAtivaService empresaAtivaService;

    @PostMapping("/selecionar")
    public String selecionar(@RequestParam Long empresaId,
                             @AuthenticationPrincipal Usuario usuario,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            empresaAtivaService.setEmpresaAtivaComValidacao(session, usuario, empresaId);
            return "redirect:/";
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getReason());
            return "redirect:/escolher-empresa";
        }
    }
}
