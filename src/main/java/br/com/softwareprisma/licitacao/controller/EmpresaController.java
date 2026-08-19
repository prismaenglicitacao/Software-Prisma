package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empresas", empresaService.listarTodas());
        return "empresas/lista";
    }

    @GetMapping("/nova")
    public String formularioNova(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresas/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("empresa") Empresa empresa, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "empresas/formulario";
        }
        empresaService.salvar(empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Empresa criada com sucesso");
        return "redirect:/empresas";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("empresa", empresaService.buscarPorId(id));
        return "empresas/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("empresa") Empresa empresa, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "empresas/formulario";
        }
        empresaService.atualizar(id, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Empresa atualizada com sucesso");
        return "redirect:/empresas";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        empresaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Empresa desativada com sucesso");
        return "redirect:/empresas";
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        empresaService.ativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Empresa ativada com sucesso");
        return "redirect:/empresas";
    }
}
