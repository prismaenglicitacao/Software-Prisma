package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import br.com.softwareprisma.licitacao.repository.UsuarioEmpresaRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaAtivaService {

    private static final String SESSION_KEY = "empresaAtivaId";

    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Transactional(readOnly = true)
    public List<Empresa> listarEmpresasDoUsuario(Usuario usuario) {
        return usuarioEmpresaRepository.findByUsuarioAtivoTrueComEmpresa(usuario)
                .stream()
                .map(ue -> ue.getEmpresa())
                .filter(e -> Boolean.TRUE.equals(e.getAtivo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Empresa getEmpresaAtiva(HttpSession session, Usuario usuario) {
        Long empresaId = (Long) session.getAttribute(SESSION_KEY);
        if (empresaId == null) {
            // Auto-select if user has exactly 1 empresa
            List<Empresa> empresas = listarEmpresasDoUsuario(usuario);
            if (empresas.size() == 1) {
                setEmpresaAtiva(session, empresas.get(0));
                return empresas.get(0);
            }
            return null;
        }
        // Re-fetch from DB to avoid stale/lazy proxy
        return empresaRepository.findById(empresaId).orElse(null);
    }

    public void setEmpresaAtiva(HttpSession session, Empresa empresa) {
        session.setAttribute(SESSION_KEY, empresa.getId());
    }

    @Transactional(readOnly = true)
    public void setEmpresaAtivaComValidacao(HttpSession session, Usuario usuario, Long empresaId) {
        boolean temAcesso = usuarioEmpresaRepository
                .existsByUsuarioAndEmpresaIdAndAtivoTrue(usuario, empresaId);
        if (!temAcesso) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não possui acesso a esta empresa");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        if (!Boolean.TRUE.equals(empresa.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Empresa inativa");
        }
        session.setAttribute(SESSION_KEY, empresaId);
    }

    public void limparEmpresaAtiva(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }
}
