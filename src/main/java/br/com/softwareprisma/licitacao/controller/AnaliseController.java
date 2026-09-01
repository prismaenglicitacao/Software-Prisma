package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.AnaliseItemForm;
import br.com.softwareprisma.licitacao.controller.form.NovaAnaliseForm;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.service.AnaliseItemService;
import br.com.softwareprisma.licitacao.service.AnaliseResultado;
import br.com.softwareprisma.licitacao.service.AnaliseService;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
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
@RequestMapping("/analises")
@RequiredArgsConstructor
public class AnaliseController {

    private final AnaliseService analiseService;
    private final AnaliseItemService analiseItemService;
    private final EmpresaAtivaService empresaAtivaService;

    @ModelAttribute("areas")
    public Area[] areas() {
        return Area.values();
    }

    @GetMapping("/nova")
    public String nova(Model model,
                       HttpSession session,
                       @AuthenticationPrincipal Usuario usuario) {
        getEmpresaOuErro(session, usuario);
        model.addAttribute("novaAnaliseForm", new NovaAnaliseForm());
        return "analises/nova";
    }

    @PostMapping
    public String iniciar(@Valid @ModelAttribute("novaAnaliseForm") NovaAnaliseForm form,
                          BindingResult bindingResult,
                          Model model,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);

        if (form.getItens().isEmpty()) {
            bindingResult.rejectValue("itens", "NotEmpty", "Informe ao menos um item.");
        }
        if (bindingResult.hasErrors()) {
            return "analises/nova";
        }

        var analise = analiseService.criar(form.getArea(), empresa, usuario);
        form.getItens().forEach(itemForm ->
                analiseItemService.salvar(analise.getId(), itemForm.toEntity()));
        return "redirect:/analises/" + analise.getId();
    }

    @PostMapping("/{id}/analisar")
    public String analisar(@PathVariable Long id,
                           HttpSession session,
                           @AuthenticationPrincipal Usuario usuario,
                           RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(id, empresa);
        AnaliseResultado resultado = analiseService.prepararAnalise(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                resultado.atende()
                        ? "Itens do edital registrados. O algoritmo indicou que a licitação atende."
                        : "Itens do edital registrados. O algoritmo indicou que a licitação não atende.");
        return "redirect:/analises/" + id + "/resumo";
    }

    @GetMapping("/{id}/resumo")
    public String resumo(@PathVariable Long id,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         Model model) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(id, empresa);
        model.addAttribute("analise", analiseService.buscarDetalhadaPorId(id));
        model.addAttribute("analiseResultado", analiseService.buscarResultadoPersistido(id));
        model.addAttribute("temSnapshotCompleto", analiseService.temSnapshotCompleto(id));
        return "analises/resumo";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          Model model) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(id, empresa);
        carregarTela(id, model, new AnaliseItemForm(), false, null);
        return "analises/detalhe";
    }

    private void carregarTela(Long analiseId,
                              Model model,
                              AnaliseItemForm itemForm,
                              boolean modoEdicao,
                              Long itemIdEdicao) {
        model.addAttribute("analise", analiseService.buscarDetalhadaPorId(analiseId));
        model.addAttribute("itemForm", itemForm);
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("itemIdEdicao", itemIdEdicao);
        model.addAttribute("acaoFormulario", modoEdicao
                ? "/analises/" + analiseId + "/itens/" + itemIdEdicao
                : "/analises/" + analiseId + "/itens");
    }

    private Empresa getEmpresaOuErro(HttpSession session, Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresa == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhuma empresa ativa selecionada.");
        }
        return empresa;
    }
}
