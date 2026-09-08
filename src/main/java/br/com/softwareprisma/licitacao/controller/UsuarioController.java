package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.form.AdminUsuarioForm;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa;
import br.com.softwareprisma.licitacao.service.EmpresaService;
import br.com.softwareprisma.licitacao.service.UsuarioEmpresaService;
import br.com.softwareprisma.licitacao.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmpresaService empresaService;
    private final UsuarioEmpresaService usuarioEmpresaService;

    @GetMapping("/meu-perfil")
    public String meuPerfil(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        return "usuarios/meu-perfil";
    }

    @PostMapping("/meu-perfil")
    public String atualizarMeuPerfil(@AuthenticationPrincipal Usuario usuario,
                                     @Valid @ModelAttribute Usuario usuarioForm,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "usuarios/meu-perfil";
        }
        usuario.setNome(usuarioForm.getNome());
        usuarioService.atualizar(usuario.getId(), usuario);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil atualizado com sucesso");
        return "redirect:/meu-perfil";
    }

    @GetMapping("/alterar-senha")
    public String alterarSenhaForm(Model model) {
        model.addAttribute("senhaForm", new SenhaForm());
        return "usuarios/alterar-senha";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@AuthenticationPrincipal Usuario usuario,
                               @Valid @ModelAttribute SenhaForm senhaForm,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "usuarios/alterar-senha";
        }

        if (!senhaForm.getNovaSenha().equals(senhaForm.getConfirmarSenha())) {
            result.rejectValue("confirmarSenha", "mismatch", "A nova senha e a confirmação devem ser iguais");
            return "usuarios/alterar-senha";
        }

        try {
            usuarioService.alterarSenhaComVerificacao(usuario.getId(), senhaForm.getSenhaAtual(), senhaForm.getNovaSenha());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso");
            return "redirect:/meu-perfil";
        } catch (IllegalArgumentException e) {
            result.rejectValue("senhaAtual", "invalid.password", e.getMessage());
            return "usuarios/alterar-senha";
        }
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/usuarios/novo")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String formularioNovo(Model model) {
        AdminUsuarioForm form = new AdminUsuarioForm();
        form.getVinculos().add(new AdminUsuarioForm.VinculoForm());
        model.addAttribute("usuarioForm", form);
        prepararFormularioNovo(model);
        model.addAttribute("tituloPagina", "Novo Usuário");
        model.addAttribute("acaoFormulario", "/usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String criar(@Valid @ModelAttribute("usuarioForm") AdminUsuarioForm form,
                       BindingResult result,
                       @AuthenticationPrincipal Usuario administrador,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            prepararFormularioNovo(model);
            return "usuarios/formulario";
        }

        if (form.getSenha().length() < 8) {
            result.rejectValue("senha", "weak.password", "A senha deve ter no mínimo 8 caracteres.");
            prepararFormularioNovo(model);
            return "usuarios/formulario";
        }
        boolean temMaiuscula = !form.getSenha().equals(form.getSenha().toLowerCase());
        boolean temMinuscula = !form.getSenha().equals(form.getSenha().toUpperCase());
        boolean temNumero = form.getSenha().matches(".*\\d.*");
        if (!temMaiuscula) {
            result.rejectValue("senha", "weak.password", "A senha deve possuir pelo menos uma letra maiúscula.");
            prepararFormularioNovo(model);
            return "usuarios/formulario";
        }
        if (!temMinuscula) {
            result.rejectValue("senha", "weak.password", "A senha deve possuir pelo menos uma letra minúscula.");
            prepararFormularioNovo(model);
            return "usuarios/formulario";
        }
        if (!temNumero) {
            result.rejectValue("senha", "weak.password", "A senha deve possuir pelo menos um número.");
            prepararFormularioNovo(model);
            return "usuarios/formulario";
        }

        try {
            List<Empresa> empresas = form.getVinculos().stream()
                    .map(vinculo -> empresaService.buscarAtivaPorId(vinculo.getEmpresaId()))
                    .collect(Collectors.toList());
            List<PerfilEmpresa> perfis = form.getVinculos().stream()
                    .map(AdminUsuarioForm.VinculoForm::getPerfil)
                    .collect(Collectors.toList());

            Usuario novo = new Usuario();
            novo.setNome(form.getNome());
            novo.setLogin(form.getLogin());
            novo.setSenha(form.getSenha());
            novo.setAtivo(true);
            novo.setAdministrador(false);
            usuarioEmpresaService.criarUsuarioComVinculos(novo, empresas, perfis, administrador);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário criado e vínculos configurados com sucesso");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException | org.springframework.web.server.ResponseStatusException e) {
            result.rejectValue("login", "duplicate.login", e.getMessage());
            prepararFormularioNovo(model);
            return "usuarios/formulario";
        }
    }

    private void prepararFormularioNovo(Model model) {
        model.addAttribute("empresas", empresaService.listarAtivas());
        model.addAttribute("perfis", PerfilEmpresa.values());
    }

    @GetMapping("/usuarios/{id}/editar")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        usuario.setSenha(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tituloPagina", "Editar Usuário");
        model.addAttribute("acaoFormulario", "/usuarios/" + id);
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String atualizar(@PathVariable Long id,
                           @Valid @ModelAttribute Usuario usuario,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "usuarios/formulario";
        }
        usuarioService.atualizar(id, usuario);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário atualizado com sucesso");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/resetar-senha")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String resetarSenha(@PathVariable Long id,
                               @RequestParam String novaSenha,
                               RedirectAttributes redirectAttributes) {
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha não pode ser vazia");
            return "redirect:/usuarios/" + id + "/editar";
        }
        
        // Validar complexidade da senha
        if (novaSenha.length() < 8) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A senha deve ter no mínimo 8 caracteres.");
            return "redirect:/usuarios/" + id + "/editar";
        }
        boolean temMaiuscula = !novaSenha.equals(novaSenha.toLowerCase());
        boolean temMinuscula = !novaSenha.equals(novaSenha.toUpperCase());
        boolean temNumero = novaSenha.matches(".*\\d.*");
        
        if (!temMaiuscula) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A senha deve possuir pelo menos uma letra maiúscula.");
            return "redirect:/usuarios/" + id + "/editar";
        }
        if (!temMinuscula) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A senha deve possuir pelo menos uma letra minúscula.");
            return "redirect:/usuarios/" + id + "/editar";
        }
        if (!temNumero) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A senha deve possuir pelo menos um número.");
            return "redirect:/usuarios/" + id + "/editar";
        }
        
        usuarioService.alterarSenha(id, novaSenha);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha resetada com sucesso");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/alternar-ativo")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.alternarAtivo(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Status do usuário alterado com sucesso");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public String excluir(@PathVariable Long id,
                          @AuthenticationPrincipal Usuario usuarioLogado,
                          RedirectAttributes redirectAttributes) {
        try {
            usuarioService.excluir(id, usuarioLogado.getId());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário excluído com sucesso");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    public static class SenhaForm {
        private String senhaAtual;

        @br.com.softwareprisma.licitacao.validation.SenhaForte
        private String novaSenha;

        private String confirmarSenha;

        public String getSenhaAtual() {
            return senhaAtual;
        }

        public void setSenhaAtual(String senhaAtual) {
            this.senhaAtual = senhaAtual;
        }

        public String getNovaSenha() {
            return novaSenha;
        }

        public void setNovaSenha(String novaSenha) {
            this.novaSenha = novaSenha;
        }

        public String getConfirmarSenha() {
            return confirmarSenha;
        }

        public void setConfirmarSenha(String confirmarSenha) {
            this.confirmarSenha = confirmarSenha;
        }
    }
}
