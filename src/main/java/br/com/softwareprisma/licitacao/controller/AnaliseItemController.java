package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.AnaliseItemForm;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.AnaliseItemService;
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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/analises/{analiseId}/itens")
@RequiredArgsConstructor
public class AnaliseItemController {

    private final AnaliseService analiseService;
    private final AnaliseItemService analiseItemService;
    private final EmpresaAtivaService empresaAtivaService;

    @PostMapping
    public String salvar(@PathVariable Long analiseId,
                         @Valid @ModelAttribute("itemForm") AnaliseItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(analiseId, empresa);
        if (bindingResult.hasErrors()) {
            carregarTela(analiseId, model, itemForm, false, null);
            return "analises/detalhe";
        }
        analiseItemService.salvar(analiseId, itemForm.toEntity());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item adicionado com sucesso.");
        return "redirect:/analises/" + analiseId;
    }

    @GetMapping("/{itemId}/editar")
    public String editar(@PathVariable Long analiseId,
                         @PathVariable Long itemId,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         Model model) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(analiseId, empresa);
        AnaliseItem item = analiseItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(analiseId, item);
        carregarTela(analiseId, model, AnaliseItemForm.fromEntity(item), true, itemId);
        return "analises/detalhe";
    }

    @PostMapping("/{itemId}")
    public String atualizar(@PathVariable Long analiseId,
                            @PathVariable Long itemId,
                            @Valid @ModelAttribute("itemForm") AnaliseItemForm itemForm,
                            BindingResult bindingResult,
                            Model model,
                            HttpSession session,
                            @AuthenticationPrincipal Usuario usuario,
                            RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(analiseId, empresa);
        AnaliseItem item = analiseItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(analiseId, item);
        if (bindingResult.hasErrors()) {
            carregarTela(analiseId, model, itemForm, true, itemId);
            return "analises/detalhe";
        }
        analiseItemService.atualizar(itemId, itemForm.toEntity());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item atualizado com sucesso.");
        return "redirect:/analises/" + analiseId;
    }

    @PostMapping("/{itemId}/excluir")
    public String excluir(@PathVariable Long analiseId,
                          @PathVariable Long itemId,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        analiseService.validarAcessoEmpresa(analiseId, empresa);
        AnaliseItem item = analiseItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(analiseId, item);
        analiseItemService.excluir(itemId);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item excluido com sucesso.");
        return "redirect:/analises/" + analiseId;
    }

    private void carregarTela(Long analiseId, Model model, AnaliseItemForm itemForm,
                              boolean modoEdicao, Long itemIdEdicao) {
        model.addAttribute("analise", analiseService.buscarDetalhadaPorId(analiseId));
        model.addAttribute("itemForm", itemForm);
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("itemIdEdicao", itemIdEdicao);
        model.addAttribute("acaoFormulario", modoEdicao
                ? "/analises/" + analiseId + "/itens/" + itemIdEdicao
                : "/analises/" + analiseId + "/itens");
    }

    private void validarPertencimento(Long analiseId, AnaliseItem item) {
        if (!item.getAnalise().getId().equals(analiseId)) {
            throw new ResponseStatusException(NOT_FOUND, "Item da analise nao encontrado");
        }
    }

    private Empresa getEmpresaOuErro(HttpSession session, Usuario usuario) {
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresa == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhuma empresa ativa selecionada.");
        }
        return empresa;
    }
}
