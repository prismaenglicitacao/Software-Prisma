package br.com.softwareprisma.licitacao.security;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostLoginEmpresaHandler implements AuthenticationSuccessHandler {

    private final EmpresaAtivaService empresaAtivaService;
    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String login = request.getParameter("username");
        loginAttemptService.loginSucceeded(login);

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) authentication.getPrincipal();

        // ADMIN SISTEMA -> area administrativa
        if (Boolean.TRUE.equals(usuario.getAdministrador())) {
            log.info("Admin {} autenticado -> /admin", usuario.getLogin());
            response.sendRedirect("/admin");
            return;
        }

        // Non-admin: 0/1/N empresa flow
        List<Empresa> empresas = empresaAtivaService.listarEmpresasDoUsuario(usuario);

        if (empresas.size() == 1) {
            empresaAtivaService.setEmpresaAtiva(session, empresas.get(0));
            log.info("Usuário {} -> empresa {} auto-selecionada", usuario.getLogin(), empresas.get(0).getNome());
            response.sendRedirect("/");
            return;
        }

        if (empresas.isEmpty()) {
            log.warn("Usuário {} sem empresa vinculada", usuario.getLogin());
            response.sendRedirect("/escolher-empresa?sem_acesso=1");
            return;
        }

        // 2+ empresas -> escolher
        log.info("Usuário {} com {} empresas -> /escolher-empresa", usuario.getLogin(), empresas.size());
        response.sendRedirect("/escolher-empresa");
    }
}
