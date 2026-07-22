package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.CatItemForm;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.service.CatItemService;
import br.com.softwareprisma.licitacao.service.CatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/cats/{catId}/itens")
@RequiredArgsConstructor
public class CatItemController {

    private static final int PAGE_SIZE = 25;

    private final CatService catService;
    private final CatItemService catItemService;

    @GetMapping
    public String listar(@PathVariable Long catId,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         Model model) {
        carregarTela(catId, model, new CatItemForm(), false, null, page, q);
        return "cat-itens/lista";
    }

    @PostMapping
    public String salvar(@PathVariable Long catId,
                         @Valid @ModelAttribute("itemForm") CatItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         @RequestParam(defaultValue = "0") int page) {
        if (bindingResult.hasErrors()) {
            carregarTela(catId, model, itemForm, false, null, page, null);
            return "cat-itens/lista";
        }

        catItemService.salvar(catId, itemForm.toEntity());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT cadastrado com sucesso.");
        return "redirect:/cats/" + catId + "/itens?page=" + page;
    }

    @GetMapping("/{itemId}/editar")
    public String editar(@PathVariable Long catId,
                         @PathVariable Long itemId,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         Model model) {
        CatItem item = catItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(catId, item);
        carregarTela(catId, model, CatItemForm.fromEntity(item), true, itemId, page, q);
        return "cat-itens/lista";
    }

    @PostMapping("/{itemId}")
    public String atualizar(@PathVariable Long catId,
                            @PathVariable Long itemId,
                            @Valid @ModelAttribute("itemForm") CatItemForm itemForm,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        CatItem item = catItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(catId, item);

        if (bindingResult.hasErrors()) {
            carregarTela(catId, model, itemForm, true, itemId, 0, null);
            return "cat-itens/lista";
        }

        catItemService.atualizar(itemId, itemForm.toEntity());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT atualizado com sucesso.");
        return "redirect:/cats/" + catId + "/itens";
    }

    @PostMapping("/{itemId}/excluir")
    public String excluir(@PathVariable Long catId,
                          @PathVariable Long itemId,
                          RedirectAttributes redirectAttributes) {
        CatItem item = catItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(catId, item);
        catItemService.excluir(itemId);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT excluido com sucesso.");
        return "redirect:/cats/" + catId + "/itens";
    }

    private void carregarTela(Long catId,
                              Model model,
                              CatItemForm itemForm,
                              boolean modoEdicao,
                              Long itemIdEdicao,
                              int page,
                              String q) {
        Cat cat = catService.buscarDetalhadaPorId(catId);

        String qNorm = (q != null && !q.isBlank()) ? q.trim() : null;
        Page<CatItem> itensPaginados = catItemService.listarPaginado(
                catId, qNorm, PageRequest.of(page, PAGE_SIZE));

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
    }

    private void validarPertencimento(Long catId, CatItem item) {
        if (!item.getCat().getId().equals(catId)) {
            throw new ResponseStatusException(NOT_FOUND, "Item da CAT nao encontrado");
        }
    }
}
