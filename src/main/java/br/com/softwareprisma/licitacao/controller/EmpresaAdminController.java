package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.EmpresaAdminUsuarioForm;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.UsuarioEmpresaService;
import br.com.softwareprisma.licitacao.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/empresa-admin")
@RequiredArgsConstructor
public class EmpresaAdminController {

    private final EmpresaAtivaService empresaAtivaService;
    private final UsuarioEmpresaService usuarioEmpresaService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String painel(HttpSession session, @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                         Model model) {
        Empresa empresa = exigirAdministrador(usuario, session);
        model.addAttribute("empresa", empresa);
        model.addAttribute("totalUsuarios", usuarioEmpresaService.listarVinculosAtivosPorEmpresa(empresa).stream()
                .filter(UsuarioEmpresa::getAtivo)
                .count());
        return "empresa-admin/index";
    }

    @GetMapping("/usuarios")
    public String usuarios(HttpSession session, @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                           Model model) {
        Empresa empresa = exigirAdministrador(usuario, session);
        model.addAttribute("empresa", empresa);
        model.addAttribute("vinculos", usuarioEmpresaService.listarVinculosAtivosPorEmpresa(empresa));
        model.addAttribute("perfis", PerfilEmpresa.values());
        return "empresa-admin/usuarios";
    }

    @GetMapping("/usuarios/novo")
    public String novo(HttpSession session, @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                       Model model) {
        Empresa empresa = exigirAdministrador(usuario, session);
        model.addAttribute("empresa", empresa);
        model.addAttribute("usuarioForm", new EmpresaAdminUsuarioForm());
        model.addAttribute("perfis", PerfilEmpresa.values());
        return "empresa-admin/usuario-formulario";
    }

    @PostMapping("/usuarios")
    public String criar(@Valid @ModelAttribute("usuarioForm") EmpresaAdminUsuarioForm form,
                        BindingResult result, HttpSession session,
                        @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                        Model model, RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        validarSenhaNova(form, result);
        if (result.hasErrors()) {
            model.addAttribute("empresa", empresa);
            model.addAttribute("perfis", PerfilEmpresa.values());
            return "empresa-admin/usuario-formulario";
        }
        try {
            usuarioEmpresaService.criarUsuarioNaEmpresa(
                    toUsuario(form), empresa, form.getPerfil(), usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário criado e vinculado à empresa.");
            return "redirect:/empresa-admin/usuarios";
        } catch (IllegalArgumentException e) {
            result.rejectValue("login", "duplicate.login", e.getMessage());
            model.addAttribute("empresa", empresa);
            model.addAttribute("perfis", PerfilEmpresa.values());
            return "empresa-admin/usuario-formulario";
        }
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editar(@PathVariable Long id, HttpSession session,
                         @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                         Model model) {
        Empresa empresa = exigirAdministrador(usuario, session);
        UsuarioEmpresa vinculo = usuarioEmpresaService.buscarVinculoPorUsuarioEEmpresa(id, empresa);
        model.addAttribute("empresa", empresa);
        model.addAttribute("vinculo", vinculo);
        model.addAttribute("usuarioForm", toForm(vinculo));
        model.addAttribute("perfis", PerfilEmpresa.values());
        return "empresa-admin/usuario-formulario";
    }

    @PostMapping("/usuarios/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("usuarioForm") EmpresaAdminUsuarioForm form,
                            BindingResult result, HttpSession session,
                            @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                            Model model, RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        UsuarioEmpresa vinculo = usuarioEmpresaService.buscarVinculoPorUsuarioEEmpresa(id, empresa);
        if (result.hasErrors()) {
            model.addAttribute("empresa", empresa);
            model.addAttribute("vinculo", vinculo);
            model.addAttribute("perfis", PerfilEmpresa.values());
            return "empresa-admin/usuario-formulario";
        }
        usuarioService.atualizarDadosEmpresa(vinculo.getUsuario().getId(), form.getNome(), form.getAtivo());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário atualizado.");
        return "redirect:/empresa-admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/senha")
    public String alterarSenha(@PathVariable Long id, @RequestParam String novaSenha,
                               HttpSession session,
                               @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                               RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        UsuarioEmpresa vinculo = usuarioEmpresaService.buscarVinculoPorUsuarioEEmpresa(id, empresa);
        validarSenha(novaSenha);
        usuarioService.alterarSenha(vinculo.getUsuario().getId(), novaSenha);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha alterada.");
        return "redirect:/empresa-admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/perfil")
    public String alterarPerfil(@PathVariable Long id, @RequestParam PerfilEmpresa perfil,
                                HttpSession session,
                                @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                                RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        usuarioEmpresaService.alterarPerfil(id, empresa, perfil);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil do vínculo atualizado.");
        return "redirect:/empresa-admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/revogar")
    public String revogar(@PathVariable Long id, HttpSession session,
                          @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                          RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        usuarioEmpresaService.revogarAcesso(id, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Vínculo revogado.");
        return "redirect:/empresa-admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/reativar")
    public String reativar(@PathVariable Long id, HttpSession session,
                           @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                           RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        usuarioEmpresaService.buscarVinculoExistentePorUsuarioEEmpresa(id, empresa);
        usuarioEmpresaService.reativarAcesso(id, empresa);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Vínculo reativado.");
        return "redirect:/empresa-admin/usuarios";
    }

    @PostMapping("/usuarios/vincular")
    public String vincular(@RequestParam String login, @RequestParam PerfilEmpresa perfil,
                           HttpSession session,
                           @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
                           RedirectAttributes redirectAttributes) {
        Empresa empresa = exigirAdministrador(usuario, session);
        Usuario alvo = usuarioService.buscarPorLogin(login);
        usuarioEmpresaService.concederAcesso(alvo, empresa, perfil, usuario);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário vinculado à empresa.");
        return "redirect:/empresa-admin/usuarios";
    }

    private Empresa exigirAdministrador(Usuario usuario, HttpSession session) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getAdministrador())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A administração global deve ser acessada pelo /admin");
        }
        Empresa empresa = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresa == null || !usuarioEmpresaService.podeAdministrarEmpresa(usuario, empresa)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para administrar esta empresa");
        }
        return empresa;
    }

    private Usuario toUsuario(EmpresaAdminUsuarioForm form) {
        Usuario novo = new Usuario();
        novo.setNome(form.getNome());
        novo.setLogin(form.getLogin());
        novo.setSenha(form.getSenha());
        novo.setAtivo(true);
        novo.setAdministrador(false);
        return novo;
    }

    private EmpresaAdminUsuarioForm toForm(UsuarioEmpresa vinculo) {
        EmpresaAdminUsuarioForm form = new EmpresaAdminUsuarioForm();
        form.setNome(vinculo.getUsuario().getNome());
        form.setLogin(vinculo.getUsuario().getLogin());
        form.setPerfil(vinculo.getPerfil());
        form.setAtivo(vinculo.getUsuario().getAtivo());
        return form;
    }

    private void validarSenhaNova(EmpresaAdminUsuarioForm form, BindingResult result) {
        if (form.getSenha() == null || form.getSenha().isBlank()) {
            result.rejectValue("senha", "required", "Informe a senha.");
            return;
        }
        try {
            validarSenha(form.getSenha());
        } catch (IllegalArgumentException e) {
            result.rejectValue("senha", "weak.password", e.getMessage());
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 8
                || senha.equals(senha.toLowerCase())
                || senha.equals(senha.toUpperCase())
                || !senha.matches(".*\\d.*")) {
            throw new IllegalArgumentException("A senha deve ter 8 caracteres, maiúscula, minúscula e número.");
        }
    }
}
