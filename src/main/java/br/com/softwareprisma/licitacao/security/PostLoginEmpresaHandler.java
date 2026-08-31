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

        // ADMIN SISTEMA always goes to /admin — never to operational dashboard
        if (Boolean.TRUE.equals(usuario.getAdministrador())) {
            log.info("Admin {} autenticado. Redirecionando para /admin.", usuario.getLogin());
            response.sendRedirect("/admin");
            return;
        }

        // Non-admin: 0/1/N empresa flow
        List<Empresa> empresasDoUsuario = empresaAtivaService.listarEmpresasDoUsuario(usuario);

        if (empresasDoUsuario.size() == 1) {
            empresaAtivaService.setEmpresaAtiva(session, empresasDoUsuario.get(0));
            log.info("Usuário {} autenticado. Empresa {} selecionada automaticamente.",
                    usuario.getLogin(), empresasDoUsuario.get(0).getNome());
            response.sendRedirect("/");
            return;
        }

        if (empresasDoUsuario.isEmpty()) {
            log.warn("Usuário {} sem empresa vinculada.", usuario.getLogin());
            response.sendRedirect("/escolher-empresa?sem_acesso=1");
            return;
        }

        // 2+ empresas
        log.info("Usuário {} com {} empresas. Redirecionando para /escolher-empresa.",
                usuario.getLogin(), empresasDoUsuario.size());
        response.sendRedirect("/escolher-empresa");
    }
}
