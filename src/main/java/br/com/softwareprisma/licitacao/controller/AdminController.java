package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import br.com.softwareprisma.licitacao.service.EmpresaService;
import br.com.softwareprisma.licitacao.service.UsuarioEmpresaService;
import br.com.softwareprisma.licitacao.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final UsuarioEmpresaService usuarioEmpresaService;

    @GetMapping
    public String painel(Model model) {
        List<Empresa> todasEmpresas = empresaService.listarTodas();
        long totalUsuarios = usuarioService.listarTodos().size();

        long totalVinculos = todasEmpresas.stream()
                .mapToLong(e -> usuarioEmpresaService.listarPorEmpresa(e).size())
                .sum();

        List<Empresa> empresasSemVinculos = todasEmpresas.stream()
                .filter(e -> usuarioEmpresaService.listarPorEmpresa(e).isEmpty())
                .toList();

        model.addAttribute("totalEmpresas", todasEmpresas.size());
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalVinculos", totalVinculos);
        model.addAttribute("empresasSemVinculos", empresasSemVinculos);
        return "admin/index";
    }

    @GetMapping("/vinculos")
    public String vinculos(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        List<Usuario> usuarios = usuarioService.listarTodos().stream()
                .filter(u -> Boolean.FALSE.equals(u.getAdministrador()))
                .toList();

        Map<Long, List<UsuarioEmpresa>> vinculos = new HashMap<>();
        for (Empresa empresa : empresas) {
            vinculos.put(empresa.getId(), usuarioEmpresaService.listarPorEmpresa(empresa));
        }

        model.addAttribute("empresas", empresas);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("vinculos", vinculos);
        return "admin/vinculos";
    }

    @PostMapping("/vinculos/conceder")
    public String concederAcesso(@RequestParam Long usuarioId,
                                 @RequestParam Long empresaId,
                                 RedirectAttributes redirectAttributes) {
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Selecione um usuário para vincular.");
            return "redirect:/admin/vinculos";
        }
        try {
            Usuario usuario = usuarioService.buscarPorId(usuarioId);
            Empresa empresa = empresaService.buscarPorId(empresaId);
            usuarioEmpresaService.concederAcesso(usuario, empresa);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Acesso de \"" + usuario.getNome() + "\" à empresa \"" + empresa.getNome() + "\" concedido com sucesso.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getReason());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/admin/vinculos";
    }

    @PostMapping("/vinculos/revogar")
    public String revogarAcesso(@RequestParam Long usuarioId,
                                @RequestParam Long empresaId,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(usuarioId);
            Empresa empresa = empresaService.buscarPorId(empresaId);
            usuarioEmpresaService.revogarAcesso(usuario, empresa);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Acesso de \"" + usuario.getNome() + "\" à empresa \"" + empresa.getNome() + "\" revogado.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getReason());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/admin/vinculos";
    }
}
