package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.CatForm;
import br.com.softwareprisma.licitacao.service.CatService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cats")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;
    private final EngenheiroService engenheiroService;

    @ModelAttribute("engenheiros")
    public Object engenheiros() {
        return engenheiroService.listarParaSelecao();
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String filtro, Model model) {
        model.addAttribute("catsPorEngenheiro", catService.listarAgrupadasPorEngenheiroComInfoFiltradas(filtro));
        model.addAttribute("filtro", filtro);
        return "cats/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("catForm", new CatForm());
        model.addAttribute("tituloPagina", "Nova CAT");
        model.addAttribute("acaoFormulario", "/cats");
        return "cats/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("catForm") CatForm catForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Nova CAT");
            model.addAttribute("acaoFormulario", "/cats");
            return "cats/formulario";
        }

        catService.salvar(catForm.toEntity(), catForm.getEngenheiroId());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "CAT cadastrada com sucesso.");
        return "redirect:/cats";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("catForm", CatForm.fromEntity(catService.buscarDetalhadaPorId(id)));
        model.addAttribute("tituloPagina", "Editar CAT");
        model.addAttribute("acaoFormulario", "/cats/" + id);
        return "cats/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("catForm") CatForm catForm,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Editar CAT");
            model.addAttribute("acaoFormulario", "/cats/" + id);
            return "cats/formulario";
        }

        catService.atualizar(id, catForm.toEntity(), catForm.getEngenheiroId());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "CAT atualizada com sucesso.");
        return "redirect:/cats";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        catService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "CAT excluida com sucesso.");
        return "redirect:/cats";
    }
}
