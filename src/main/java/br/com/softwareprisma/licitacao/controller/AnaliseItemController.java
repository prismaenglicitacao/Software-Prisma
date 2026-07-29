package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.AnaliseItemForm;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.service.AnaliseItemService;
import br.com.softwareprisma.licitacao.service.AnaliseService;
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
@RequestMapping("/analises/{analiseId}/itens")
@RequiredArgsConstructor
public class AnaliseItemController {

    private final AnaliseService analiseService;
    private final AnaliseItemService analiseItemService;

    @PostMapping
    public String salvar(@PathVariable Long analiseId,
                         @Valid @ModelAttribute("itemForm") AnaliseItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        System.out.println("=== AnaliseItemController.salvar() INICIO ===");
        System.out.println("Analise ID: " + analiseId);
        System.out.println("ItemForm: " + itemForm.getDescricao() + ", " + itemForm.getQuantidade() + ", " + itemForm.getUnidade());
        
        if (bindingResult.hasErrors()) {
            System.out.println("Erros de validação encontrados");
            carregarTela(analiseId, model, itemForm, false, null);
            return "analises/detalhe";
        }

        System.out.println("Chamando analiseItemService.salvar()");
        analiseItemService.salvar(analiseId, itemForm.toEntity());
        System.out.println("=== AnaliseItemController.salvar() FIM ===");
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item adicionado com sucesso.");
        return "redirect:/analises/" + analiseId;
    }

    @GetMapping("/{itemId}/editar")
    public String editar(@PathVariable Long analiseId, @PathVariable Long itemId, Model model) {
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
                            RedirectAttributes redirectAttributes) {
        System.out.println("=== AnaliseItemController.atualizar() INICIO ===");
        System.out.println("Analise ID: " + analiseId);
        System.out.println("Item ID: " + itemId);
        System.out.println("ItemForm: " + itemForm.getDescricao() + ", " + itemForm.getQuantidade() + ", " + itemForm.getUnidade());
        
        AnaliseItem item = analiseItemService.buscarDetalhadoPorId(itemId);
        validarPertencimento(analiseId, item);

        if (bindingResult.hasErrors()) {
            System.out.println("Erros de validação encontrados");
            carregarTela(analiseId, model, itemForm, true, itemId);
            return "analises/detalhe";
        }

        System.out.println("Chamando analiseItemService.atualizar()");
        analiseItemService.atualizar(itemId, itemForm.toEntity());
        System.out.println("=== AnaliseItemController.atualizar() FIM ===");
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item atualizado com sucesso.");
        return "redirect:/analises/" + analiseId;
    }

    @PostMapping("/{itemId}/excluir")
    public String excluir(@PathVariable Long analiseId,
                          @PathVariable Long itemId,
                          RedirectAttributes redirectAttributes) {
        System.out.println("=== AnaliseItemController.excluir() INICIO ===");
        System.out.println("Analise ID: " + analiseId);
        System.out.println("Item ID: " + itemId);
        
        AnaliseItem item = analiseItemService.buscarDetalhadoPorId(itemId);
        System.out.println("Item encontrado: " + item.getDescricao() + ", " + item.getQuantidade() + ", " + item.getUnidade());
        validarPertencimento(analiseId, item);
        
        System.out.println("Chamando analiseItemService.excluir()");
        analiseItemService.excluir(itemId);
        System.out.println("=== AnaliseItemController.excluir() FIM ===");
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Item excluido com sucesso.");
        return "redirect:/analises/" + analiseId;
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

    private void validarPertencimento(Long analiseId, AnaliseItem item) {
        if (!item.getAnalise().getId().equals(analiseId)) {
            throw new ResponseStatusException(NOT_FOUND, "Item da analise nao encontrado");
        }
    }
}
