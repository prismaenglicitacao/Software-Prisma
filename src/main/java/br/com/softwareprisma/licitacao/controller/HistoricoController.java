package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService historicoService;

    @GetMapping
    public String historico(
            Model model,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String resultado) {
        
        Page<Analise> analises = historicoService.buscarAnalises(pagina, ordenarPor, area, resultado);
        
        model.addAttribute("analises", analises.getContent());
        model.addAttribute("pagina", analises);
        model.addAttribute("ordenarPor", ordenarPor != null ? ordenarPor : "data");
        model.addAttribute("area", area);
        model.addAttribute("resultado", resultado);
        
        return "historico/index";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            historicoService.excluirAnalise(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Análise excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir análise: " + e.getMessage());
        }
        return "redirect:/historico";
    }
}
