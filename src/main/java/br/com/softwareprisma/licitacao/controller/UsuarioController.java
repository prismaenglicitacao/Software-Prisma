package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Usuario;
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

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

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
    @PreAuthorize("hasRole('ADMIN')")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/usuarios/novo")
    @PreAuthorize("hasRole('ADMIN')")
    public String formularioNovo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tituloPagina", "Novo Usuário");
        model.addAttribute("acaoFormulario", "/usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public String criar(@Valid @ModelAttribute Usuario usuario,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "usuarios/formulario";
        }
        try {
            usuarioService.criar(usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário criado com sucesso");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            result.rejectValue("login", "duplicate.login", e.getMessage());
            return "usuarios/formulario";
        }
    }

    @GetMapping("/usuarios/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        usuario.setSenha(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tituloPagina", "Editar Usuário");
        model.addAttribute("acaoFormulario", "/usuarios/" + id);
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public String resetarSenha(@PathVariable Long id,
                               @RequestParam String novaSenha,
                               RedirectAttributes redirectAttributes) {
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha não pode ser vazia");
            return "redirect:/usuarios/" + id + "/editar";
        }
        usuarioService.alterarSenha(id, novaSenha);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha resetada com sucesso");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/alternar-ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.alternarAtivo(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Status do usuário alterado com sucesso");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
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
