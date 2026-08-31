package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaAtivaService {

    private static final String EMPRESA_ATIVA_SESSION_KEY = "empresaAtivaId";

    private final UsuarioEmpresaService usuarioEmpresaService;
    private final EmpresaService empresaService;

    /**
     * Obtém a empresa ativa da sessão do usuário.
     * Se não houver empresa ativa, tenta selecionar automaticamente a primeira empresa do usuário.
     */
    public Empresa getEmpresaAtiva(HttpSession session, Usuario usuario) {
        Long empresaAtivaId = (Long) session.getAttribute(EMPRESA_ATIVA_SESSION_KEY);

        if (empresaAtivaId != null) {
            Empresa empresa = empresaService.buscarPorId(empresaAtivaId);
            if (empresa != null && empresa.getAtivo()) {
                // Verificar se o usuário ainda tem acesso a essa empresa
                if (usuarioEmpresaService.usuarioTemAcesso(usuario, empresa)) {
                    return empresa;
                } else {
                    // Usuário perdeu acesso, limpar a empresa ativa
                    limparEmpresaAtiva(session);
                    log.warn("Usuário {} perdeu acesso à empresa ativa {}", usuario.getLogin(), empresaAtivaId);
                }
            } else {
                // Empresa não existe ou está inativa, limpar
                limparEmpresaAtiva(session);
                log.warn("Empresa ativa {} não existe ou está inativa", empresaAtivaId);
            }
        }

        // Tentar selecionar automaticamente a primeira empresa do usuário
        List<UsuarioEmpresa> empresasDoUsuario = usuarioEmpresaService.listarPorUsuario(usuario);
        if (!empresasDoUsuario.isEmpty()) {
            for (UsuarioEmpresa usuarioEmpresa : empresasDoUsuario) {
                if (usuarioEmpresa.getAtivo() && usuarioEmpresa.getEmpresa().getAtivo()) {
                    setEmpresaAtiva(session, usuarioEmpresa.getEmpresa());
                    log.info("Empresa {} selecionada automaticamente para usuário {}", 
                            usuarioEmpresa.getEmpresa().getNome(), usuario.getLogin());
                    return usuarioEmpresa.getEmpresa();
                }
            }
        }

        return null;
    }

    /**
     * Define a empresa ativa na sessão do usuário.
     * Valida se o usuário tem acesso à empresa.
     */
    public void setEmpresaAtiva(HttpSession session, Empresa empresa) {
        session.setAttribute(EMPRESA_ATIVA_SESSION_KEY, empresa.getId());
        log.debug("Empresa ativa definida: {}", empresa.getNome());
    }

    /**
     * Define a empresa ativa na sessão do usuário, validando o acesso.
     */
    public void setEmpresaAtivaComValidacao(HttpSession session, Usuario usuario, Long empresaId) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        if (empresa == null) {
            throw new ResponseStatusException(NOT_FOUND, "Empresa não encontrada");
        }

        if (!empresa.getAtivo()) {
            throw new ResponseStatusException(FORBIDDEN, "Empresa não está ativa");
        }

        if (!usuarioEmpresaService.usuarioTemAcesso(usuario, empresa)) {
            throw new ResponseStatusException(FORBIDDEN, "Você não tem acesso a esta empresa");
        }

        setEmpresaAtiva(session, empresa);
        log.info("Usuário {} selecionou empresa {}", usuario.getLogin(), empresa.getNome());
    }

    /**
     * Limpa a empresa ativa da sessão.
     */
    public void limparEmpresaAtiva(HttpSession session) {
        session.removeAttribute(EMPRESA_ATIVA_SESSION_KEY);
        log.debug("Empresa ativa limpa da sessão");
    }

    /**
     * Verifica se o usuário tem uma empresa ativa na sessão.
     */
    public boolean temEmpresaAtiva(HttpSession session) {
        return session.getAttribute(EMPRESA_ATIVA_SESSION_KEY) != null;
    }

    /**
     * Obtém o ID da empresa ativa da sessão.
     */
    public Long getEmpresaAtivaId(HttpSession session) {
        return (Long) session.getAttribute(EMPRESA_ATIVA_SESSION_KEY);
    }

    /**
     * Lista todas as empresas às quais o usuário tem acesso.
     */
    public List<Empresa> listarEmpresasDoUsuario(Usuario usuario) {
        return usuarioEmpresaService.listarPorUsuario(usuario).stream()
                .filter(ue -> ue.getAtivo() && ue.getEmpresa().getAtivo())
                .map(UsuarioEmpresa::getEmpresa)
                .toList();
    }
}
