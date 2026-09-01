package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.CatItemForm;
import br.com.softwareprisma.licitacao.controller.form.CatItemLoteForm;
import br.com.softwareprisma.licitacao.controller.form.CatItemLoteResultado;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.CatItemLoteParser;
import br.com.softwareprisma.licitacao.service.CatItemService;
import br.com.softwareprisma.licitacao.service.CatService;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/cats/{catId}/itens")
@RequiredArgsConstructor
public class CatItemController {

    private static final int PAGE_SIZE = 25;

    private final CatService catService;
    private final CatItemService catItemService;
    private final CatItemLoteParser catItemLoteParser;
    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping
    public String listar(@PathVariable Long catId,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         Model model) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        carregarTela(catId, empresa, model, new CatItemForm(), false, null, page, q);
        return "cat-itens/lista";
    }

    @PostMapping
    public String salvar(@PathVariable Long catId,
                         @Valid @ModelAttribute("itemForm") CatItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(defaultValue = "0") int page) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        if (bindingResult.hasErrors()) {
            carregarTela(catId, empresa, model, itemForm, false, null, page, null);
            return "cat-itens/lista";
        }
        catItemService.salvarEEmpresa(catId, itemForm.toEntity(), empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT cadastrado com sucesso.");
        return "redirect:/cats/" + catId + "/itens?page=" + page;
    }

    @GetMapping("/{itemId}/editar")
    public String editar(@PathVariable Long catId,
                         @PathVariable Long itemId,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         HttpSession session,
                         @AuthenticationPrincipal Usuario usuario,
                         Model model) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        CatItem item = catItemService.buscarDetalhadoPorIdEEmpresa(itemId, empresa);
        validarPertencimento(catId, item);
        carregarTela(catId, empresa, model, CatItemForm.fromEntity(item), true, itemId, page, q);
        return "cat-itens/lista";
    }

    @PostMapping("/{itemId}")
    public String atualizar(@PathVariable Long catId,
                            @PathVariable Long itemId,
                            @Valid @ModelAttribute("itemForm") CatItemForm itemForm,
                            BindingResult bindingResult,
                            Model model,
                            HttpSession session,
                            @AuthenticationPrincipal Usuario usuario,
                            RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        CatItem item = catItemService.buscarDetalhadoPorIdEEmpresa(itemId, empresa);
        validarPertencimento(catId, item);
        if (bindingResult.hasErrors()) {
            carregarTela(catId, empresa, model, itemForm, true, itemId, 0, null);
            return "cat-itens/lista";
        }
        catItemService.atualizarEEmpresa(itemId, itemForm.toEntity(), empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT atualizado com sucesso.");
        return "redirect:/cats/" + catId + "/itens";
    }

    @PostMapping("/{itemId}/excluir")
    public String excluir(@PathVariable Long catId,
                          @PathVariable Long itemId,
                          HttpSession session,
                          @AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        CatItem item = catItemService.buscarDetalhadoPorIdEEmpresa(itemId, empresa);
        validarPertencimento(catId, item);
        catItemService.excluirEEmpresa(itemId, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT excluido com sucesso.");
        return "redirect:/cats/" + catId + "/itens";
    }

    @PostMapping("/lote")
    public String salvarLote(@PathVariable Long catId,
                             @Valid @ModelAttribute("loteForm") CatItemLoteForm loteForm,
                             BindingResult bindingResult,
                             Model model,
                             HttpSession session,
                             @AuthenticationPrincipal Usuario usuario,
                             RedirectAttributes redirectAttributes) {
        Empresa empresa = getEmpresaOuErro(session, usuario);
        if (bindingResult.hasErrors()) {
            carregarTela(catId, empresa, model, new CatItemForm(), false, null, 0, null);
            model.addAttribute("loteForm", loteForm);
            model.addAttribute("mostrarModalLote", true);
            return "cat-itens/lista";
        }

        CatItemLoteResultado resultado = catItemLoteParser.parse(loteForm.getItensTexto());

        if (!resultado.getItensValidos().isEmpty()) {
            catItemService.salvarEmLoteEEmpresa(catId, resultado.getItensValidos(), empresa);
        }

        StringBuilder mensagem = new StringBuilder();
        mensagem.append(resultado.getItensCadastrados()).append(" item(ns) cadastrado(s) com sucesso.");
        if (resultado.getItensComErro() > 0) {
            mensagem.append(" ").append(resultado.getItensComErro()).append(" item(ns) com erro:");
            for (CatItemLoteResultado.ErroLinha erro : resultado.getErros()) {
                mensagem.append(" Linha ").append(erro.getNumeroLinha()).append(": ").append(erro.getMotivo()).append(";");
            }
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", mensagem.toString());
        return "redirect:/cats/" + catId + "/itens";
    }

    private void carregarTela(Long catId,
                              Empresa empresa,
                              Model model,
                              CatItemForm itemForm,
                              boolean modoEdicao,
                              Long itemIdEdicao,
                              int page,
                              String q) {
        Cat cat = catService.buscarDetalhadaPorIdEEmpresa(catId, empresa);

        String qNorm = (q != null && !q.isBlank()) ? q.trim() : null;
        Page<CatItem> itensPaginados = catItemService.listarPaginadoEEmpresa(
                catId, empresa, qNorm, PageRequest.of(page, PAGE_SIZE));

        model.addAttribute("cat", cat);
        model.addAttribute("itensPaginados", itensPaginados);
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("paginaAtual", page);
        model.addAttribute("itemForm", itemForm);
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("itemIdEdicao", itemIdEdicao);
        model.addAttribute("acaoFormulario", modoEdicao
                ? "/cats/" + catId + "/itens/" + itemIdEdicao
                : "/cats/" + catId + "/itens");

        if (!model.containsAttribute("loteForm")) {
            model.addAttribute("loteForm", new CatItemLoteForm());
        }
    }

    private void validarPertencimento(Long catId, CatItem item) {
        if (!item.getCat().getId().equals(catId)) {
            throw new ResponseStatusException(NOT_FOUND, "Item da CAT nao encontrado");
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
