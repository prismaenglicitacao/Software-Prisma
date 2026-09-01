package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import br.com.softwareprisma.licitacao.repository.UsuarioEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UsuarioEmpresaService {

    private final UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> listarPorUsuario(Usuario usuario) {
        return usuarioEmpresaRepository.findByUsuarioAtivoTrueComEmpresa(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> listarPorEmpresa(Empresa empresa) {
        return usuarioEmpresaRepository.findByEmpresaAtivoTrueComUsuario(empresa);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioEmpresa> buscarPorUsuarioEEmpresa(Usuario usuario, Empresa empresa) {
        return usuarioEmpresaRepository.findByUsuarioAndEmpresaAndAtivoTrue(usuario, empresa);
    }

    @Transactional(readOnly = true)
    public boolean usuarioTemAcesso(Usuario usuario, Empresa empresa) {
        return usuarioEmpresaRepository.existsByUsuarioAndEmpresaAndAtivoTrue(usuario, empresa);
    }

    @Transactional
    public UsuarioEmpresa concederAcesso(Usuario usuario, Empresa empresa, Usuario concedidoPor) {
        Optional<UsuarioEmpresa> existente = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa);

        if (existente.isPresent()) {
            UsuarioEmpresa usuarioEmpresa = existente.get();
            if (usuarioEmpresa.getAtivo()) {
                throw new ResponseStatusException(BAD_REQUEST, "Usuário já possui acesso a esta empresa");
            }
            // Reativar vínculo existente
            usuarioEmpresa.setAtivo(true);
            usuarioEmpresa.setConcedidoPor(concedidoPor);
            return usuarioEmpresaRepository.save(usuarioEmpresa);
        }

        // Criar novo vínculo
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(usuario);
        usuarioEmpresa.setEmpresa(empresa);
        usuarioEmpresa.setAtivo(true);
        usuarioEmpresa.setConcedidoPor(concedidoPor);

        return usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    @Transactional
    public UsuarioEmpresa concederAcesso(Usuario usuario, Empresa empresa) {
        return concederAcesso(usuario, empresa, null);
    }

    @Transactional
    public void revogarAcesso(Usuario usuario, Empresa empresa) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioAndEmpresaAndAtivoTrue(usuario, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vínculo não encontrado"));

        usuarioEmpresa.setAtivo(false);
        usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    @Transactional
    public void reativarAcesso(Usuario usuario, Empresa empresa) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vínculo não encontrado"));

        usuarioEmpresa.setAtivo(true);
        usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    @Transactional
    public void removerVinculo(Usuario usuario, Empresa empresa) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vínculo não encontrado"));
        usuarioEmpresaRepository.delete(usuarioEmpresa);
    }
}
