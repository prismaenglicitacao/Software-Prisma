package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.CatItemForm;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.service.CatItemService;
import br.com.softwareprisma.licitacao.service.CatService;
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
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/cats/{catId}/itens")
@RequiredArgsConstructor
public class CatItemController {

    private final CatService catService;
    private final CatItemService catItemService;

    @GetMapping
    public String listar(@PathVariable Long catId, Model model) {
        carregarTela(catId, model, new CatItemForm(), false, null);
        return "cat-itens/lista";
    }

    @PostMapping
    public String salvar(@PathVariable Long catId,
                         @Valid @ModelAttribute("itemForm") CatItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            carregarTela(catId, model, itemForm, false, null);
            return "cat-itens/lista";
        }

        catItemService.salvar(catId, itemForm.toEntity());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item da CAT cadastrado com sucesso.");
        return "redirect:/cats/" + catId + "/itens";
    }

    @GetMapping("/{itemId}/editar")
    public String editar(@PathVariable Long catId, @PathVariable Long itemId, Model model) {
        CatItem item = catItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(catId, item);
        carregarTela(catId, model, CatItemForm.fromEntity(item), true, itemId);
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
            carregarTela(catId, model, itemForm, true, itemId);
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
                              Long itemIdEdicao) {
        Cat cat = catService.buscarDetalhadaPorId(catId);
        model.addAttribute("cat", cat);
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
