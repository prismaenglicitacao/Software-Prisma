package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new IllegalArgumentException("Login já cadastrado");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario usuario) {
        Usuario existente = buscarPorId(id);
        existente.setNome(usuario.getNome());
        existente.setAtivo(usuario.getAtivo());
        existente.setAdministrador(usuario.getAdministrador());
        return usuarioRepository.save(existente);
    }

    @Transactional
    public void alterarSenha(Long id, String novaSenha) {
        Usuario usuario = buscarPorId(id);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenhaComVerificacao(Long id, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarPorId(id);
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void alternarAtivo(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(!usuario.getAtivo());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(Long id, Long idUsuarioLogado) {
        if (id.equals(idUsuarioLogado)) {
            throw new IllegalArgumentException("Não é possível excluir o próprio usuário");
        }
        
        Usuario usuario = buscarPorId(id);
        
        if (Boolean.TRUE.equals(usuario.getAdministrador())) {
            long totalAdmins = usuarioRepository.findAll().stream()
                    .filter(Usuario::getAdministrador)
                    .count();
            if (totalAdmins <= 1) {
                throw new IllegalArgumentException("Não é possível excluir o último administrador do sistema");
            }
        }
        
        usuarioRepository.delete(usuario);
    }

    public boolean existeAdmin() {
        return usuarioRepository.findAll().stream()
                .anyMatch(Usuario::getAdministrador);
    }

    @Transactional
    public Usuario criarAdminPadrao() {
        if (existeAdmin()) {
            throw new IllegalStateException("Já existe um administrador cadastrado");
        }
        
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setLogin("admin");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setAtivo(true);
        admin.setAdministrador(true);
        
        return usuarioRepository.save(admin);
    }
}
