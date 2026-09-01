package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.enums.Area;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/engenheiros")
@RequiredArgsConstructor
public class EngenheiroController {

    private final EngenheiroService engenheiroService;
    private final EmpresaAtivaService empresaAtivaService;

    @ModelAttribute("areas")
    public Area[] areas() {
        return Area.values();
    }

    @GetMapping
    public String listar(Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        model.addAttribute("engenheiros", engenheiroService.listarPorEmpresa(empresa));
        return "engenheiros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model,
                       HttpSession session,
                       @AuthenticationPrincipal Usuario usuario) {
        getEmpresaOuErro(session, usuario);
        model.addAttribute("engenheiro", new Engenheiro());
        model.addAttribute("tituloPagina", "Novo Engenheiro");
        model.addAttribute("acaoFormulario", "/engenheiros");
        return "engenheiros/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("engenheiro") Engenheiro engenheiro,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Novo Engenheiro");
            model.addAttribute("acaoFormulario", "/engenheiros");
            return "engenheiros/formulario";
        }
        engenheiroService.salvarComEmpresa(engenheiro, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Engenheiro cadastrado com sucesso.");
        return "redirect:/engenheiros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        model.addAttribute("engenheiro", engenheiroService.buscarPorIdEEmpresa(id, empresa));
        model.addAttribute("tituloPagina", "Editar Engenheiro");
        model.addAttribute("acaoFormulario", "/engenheiros/" + id);
        return "engenheiros/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("engenheiro") Engenheiro engenheiro,
                            BindingResult bindingResult,
                            Model model,
                            HttpSession session,
                            @AuthenticationPrincipal Usuario usuario,
                            RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", "Editar Engenheiro");
            model.addAttribute("acaoFormulario", "/engenheiros/" + id);
            return "engenheiros/formulario";
        }
        engenheiroService.atualizarComEmpresa(id, engenheiro, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Engenheiro atualizado com sucesso.");
        return "redirect:/engenheiros";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        engenheiroService.excluirPorIdEEmpresa(id, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Engenheiro excluido com sucesso.");
        return "redirect:/engenheiros";
    }

    private Empresa getEmpresaOuErro(HttpSession session, Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresa == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhuma empresa ativa selecionada.");
        }
        return empresa;
    }
}
