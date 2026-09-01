package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.CatForm;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.CatService;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.EngenheiroService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cats")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;
    private final EngenheiroService engenheiroService;
    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping
    public String listar(@RequestParam(required = false) String filtro,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        model.addAttribute("catsPorEngenheiro",
                catService.listarAgrupadasPorEngenheiroComInfoFiltradas(filtro, empresa));
        model.addAttribute("filtro", filtro);
        return "cats/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model,
                       HttpSession session,
                       @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        model.addAttribute("catForm", new CatForm());
        model.addAttribute("engenheiros", engenheiroService.listarParaSelecaoPorEmpresa(empresa));
        model.addAttribute("tituloPagina", "Nova CAT");
        model.addAttribute("acaoFormulario", "/cats");
        return "cats/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("catForm") CatForm catForm,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        if (bindingResult.hasErrors()) {
            model.addAttribute("engenheiros", engenheiroService.listarParaSelecaoPorEmpresa(empresa));
            model.addAttribute("tituloPagina", "Nova CAT");
            model.addAttribute("acaoFormulario", "/cats");
            return "cats/formulario";
        }
        catService.salvarComEmpresa(catForm.toEntity(), catForm.getEngenheiroId(), empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "CAT cadastrada com sucesso.");
        return "redirect:/cats";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        model.addAttribute("catForm", CatForm.fromEntity(catService.buscarDetalhadaPorIdEEmpresa(id, empresa)));
        model.addAttribute("engenheiros", engenheiroService.listarParaSelecaoPorEmpresa(empresa));
        model.addAttribute("tituloPagina", "Editar CAT");
        model.addAttribute("acaoFormulario", "/cats/" + id);
        return "cats/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("catForm") CatForm catForm,
                            BindingResult bindingResult,
                            Model model,
                            HttpSession session,
                            @AuthenticationPrincipal Usuario usuario,
                            RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        if (bindingResult.hasErrors()) {
            model.addAttribute("engenheiros", engenheiroService.listarParaSelecaoPorEmpresa(empresa));
            model.addAttribute("tituloPagina", "Editar CAT");
            model.addAttribute("acaoFormulario", "/cats/" + id);
            return "cats/formulario";
        }
        catService.atualizarComEmpresa(id, catForm.toEntity(), catForm.getEngenheiroId(), empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "CAT atualizada com sucesso.");
        return "redirect:/cats";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        catService.excluirPorIdEEmpresa(id, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "CAT excluida com sucesso.");
        return "redirect:/cats";
    }

    private Empresa getEmpresaOuErro(HttpSession session, Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresa == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhuma empresa ativa selecionada.");
        }
        return empresa;
    }
}
