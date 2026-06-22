package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.service.EngenheiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/engenheiros")
@RequiredArgsConstructor
public class EngenheiroController {

    private final EngenheiroService engenheiroService;

    @ModelAttribute("areas")
    public Area[] areas() {
        return Area.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("engenheiros", engenheiroService.listarTodos());
        return "engenheiros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("engenheiro", new Engenheiro());
        model.addAttribute("tituloPagina", "Novo Engenheiro");
        model.addAttribute("acaoFormulario", "/engenheiros");
        return "engenheiros/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("engenheiro") Engenheiro engenheiro,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Novo Engenheiro");
            model.addAttribute("acaoFormulario", "/engenheiros");
            return "engenheiros/formulario";
        }

        engenheiroService.salvar(engenheiro);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Engenheiro cadastrado com sucesso.");
        return "redirect:/engenheiros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("engenheiro", engenheiroService.buscarPorId(id));
        model.addAttribute("tituloPagina", "Editar Engenheiro");
        model.addAttribute("acaoFormulario", "/engenheiros/" + id);
        return "engenheiros/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("engenheiro") Engenheiro engenheiro,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Editar Engenheiro");
            model.addAttribute("acaoFormulario", "/engenheiros/" + id);
            return "engenheiros/formulario";
        }

        engenheiroService.atualizar(id, engenheiro);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Engenheiro atualizado com sucesso.");
        return "redirect:/engenheiros";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        engenheiroService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Engenheiro excluido com sucesso.");
        return "redirect:/engenheiros";
    }
}
