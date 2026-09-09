package br.com.softwareprisma.licitacao.controller.advice;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.UsuarioEmpresaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class EmpresaAtivaControllerAdvice {

    private final EmpresaAtivaService empresaAtivaService;
    private final UsuarioEmpresaService usuarioEmpresaService;

    @ModelAttribute("empresaAtiva")
    public Empresa empresaAtiva(@AuthenticationPrincipal Usuario usuario,
                                HttpSession session) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getAdministrador())) {
            return null;
        }
        return empresaAtivaService.getEmpresaAtiva(session, usuario);
    }

    @ModelAttribute("empresasDoUsuario")
    public List<Empresa> empresasDoUsuario(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getAdministrador())) {
            return List.of();
        }
        return empresaAtivaService.listarEmpresasDoUsuario(usuario);
    }

    @ModelAttribute("podeAdministrarEmpresaAtiva")
    public boolean podeAdministrarEmpresaAtiva(@AuthenticationPrincipal Usuario usuario,
                                                HttpSession session) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getAdministrador())) {
            return false;
        }
        return usuarioEmpresaService.podeAdministrarEmpresa(
                usuario, empresaAtivaService.getEmpresaAtiva(session, usuario));
    }
}
