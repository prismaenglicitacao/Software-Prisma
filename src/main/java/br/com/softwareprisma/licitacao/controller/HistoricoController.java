package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.HistoricoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService historicoService;
    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping
    public String historico(Model model,
                            @RequestParam(required = false) Integer pagina,
                            @RequestParam(required = false) String ordenarPor,
                            @RequestParam(required = false) String area,
                            @RequestParam(required = false) String resultado,
                            HttpSession session,
                            @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        Page<Analise> analises = historicoService.buscarAnalises(pagina, ordenarPor, area, resultado, empresa);

        model.addAttribute("analises", analises.getContent());
        model.addAttribute("pagina", analises);
        model.addAttribute("ordenarPor", ordenarPor != null ? ordenarPor : "data");
        model.addAttribute("area", area);
        model.addAttribute("resultado", resultado);

        return "historico/index";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        try {
            historicoService.excluirAnalise(id, empresa);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Análise excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir análise: " + e.getMessage());
        }
        return "redirect:/historico";
    }

    private Empresa getEmpresaOuErro(HttpSession session, Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresa == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhuma empresa ativa selecionada.");
        }
        return empresa;
    }
}
