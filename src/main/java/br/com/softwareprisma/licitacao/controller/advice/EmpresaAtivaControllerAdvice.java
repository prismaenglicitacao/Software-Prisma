package br.com.softwareprisma.licitacao.controller.advice;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
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

    @ModelAttribute("empresaAtiva")
    public Empresa empresaAtiva(@AuthenticationPrincipal Usuario usuario,
                                HttpSession session) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getAdministrador())) {
            return null;
        }
        try {
            return empresaAtivaService.getEmpresaAtiva(session, usuario);
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("empresasDoUsuario")
    public List<Empresa> empresasDoUsuario(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getAdministrador())) {
            return List.of();
        }
        try {
            return empresaAtivaService.listarEmpresasDoUsuario(usuario);
        } catch (Exception e) {
            return List.of();
        }
    }
}
