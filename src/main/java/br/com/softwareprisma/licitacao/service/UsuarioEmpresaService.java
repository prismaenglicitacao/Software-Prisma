package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa;
import br.com.softwareprisma.licitacao.repository.UsuarioEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UsuarioEmpresaService {

    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioService usuarioService;

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

    @Transactional(readOnly = true)
    public boolean podeAdministrarEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario == null || empresa == null) {
            return false;
        }
        if (Boolean.TRUE.equals(usuario.getAdministrador())) {
            return true;
        }
        return buscarPorUsuarioEEmpresa(usuario, empresa)
                .map(vinculo -> vinculo.getPerfil() == PerfilEmpresa.ADMIN_EMPRESA)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> listarVinculosAtivosPorEmpresa(Empresa empresa) {
        return usuarioEmpresaRepository.findByEmpresaComUsuario(empresa);
    }

    @Transactional(readOnly = true)
    public UsuarioEmpresa buscarVinculoPorUsuarioEEmpresa(Long usuarioId, Empresa empresa) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresaAndAtivoTrue(usuarioId, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não possui vínculo ativo com esta empresa"));
    }

    @Transactional(readOnly = true)
    public UsuarioEmpresa buscarVinculoExistentePorUsuarioEEmpresa(Long usuarioId, Empresa empresa) {
        return usuarioEmpresaRepository.findByUsuarioIdAndEmpresa(usuarioId, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vínculo não encontrado"));
    }

    @Transactional
    public Usuario criarUsuarioNaEmpresa(Usuario usuario, Empresa empresa,
                                          PerfilEmpresa perfil, Usuario concedidoPor) {
        usuario.setAdministrador(false);
        Usuario criado = usuarioService.criar(usuario);
        concederAcesso(criado, empresa, perfil, concedidoPor);
        return criado;
    }

    @Transactional
    public Usuario criarUsuarioComVinculos(Usuario usuario, List<Empresa> empresas,
                                           List<PerfilEmpresa> perfis, Usuario concedidoPor) {
        if (empresas == null || perfis == null || empresas.isEmpty()
                || empresas.size() != perfis.size()) {
            throw new IllegalArgumentException("Informe ao menos uma empresa e um perfil para cada vínculo");
        }

        Set<Long> empresasInformadas = new HashSet<>();
        for (Empresa empresa : empresas) {
            if (empresa == null || !empresasInformadas.add(empresa.getId())) {
                throw new IllegalArgumentException("Não é permitido repetir uma empresa no cadastro");
            }
        }

        usuario.setAdministrador(false);
        Usuario criado = usuarioService.criar(usuario);
        for (int i = 0; i < empresas.size(); i++) {
            PerfilEmpresa perfil = perfis.get(i);
            if (perfil == null) {
                throw new IllegalArgumentException("Perfil de empresa inválido");
            }
            concederAcesso(criado, empresas.get(i), perfil, concedidoPor);
        }
        return criado;
    }

    @Transactional
    public UsuarioEmpresa concederAcesso(Usuario usuario, Empresa empresa, Usuario concedidoPor) {
        return concederAcesso(usuario, empresa, PerfilEmpresa.USUARIO, concedidoPor);
    }

    @Transactional
    public UsuarioEmpresa concederAcesso(Usuario usuario, Empresa empresa,
                                         PerfilEmpresa perfil, Usuario concedidoPor) {
        Optional<UsuarioEmpresa> existente = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuario, empresa);

        if (existente.isPresent()) {
            UsuarioEmpresa usuarioEmpresa = existente.get();
            if (usuarioEmpresa.getAtivo()) {
                throw new ResponseStatusException(BAD_REQUEST, "Usuário já possui acesso a esta empresa");
            }
            // Reativar vínculo existente
            usuarioEmpresa.setAtivo(true);
            usuarioEmpresa.setPerfil(perfil);
            usuarioEmpresa.setConcedidoPor(concedidoPor);
            return usuarioEmpresaRepository.save(usuarioEmpresa);
        }

        // Criar novo vínculo
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(usuario);
        usuarioEmpresa.setEmpresa(empresa);
        usuarioEmpresa.setAtivo(true);
        usuarioEmpresa.setPerfil(perfil);
        usuarioEmpresa.setConcedidoPor(concedidoPor);

        return usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    @Transactional
    public UsuarioEmpresa concederAcesso(Usuario usuario, Empresa empresa) {
        return concederAcesso(usuario, empresa, null);
    }

    @Transactional
    public void alterarPerfil(Long usuarioId, Empresa empresa, PerfilEmpresa perfil) {
        UsuarioEmpresa vinculo = buscarVinculoPorUsuarioEEmpresa(usuarioId, empresa);
        if (vinculo.getPerfil() == PerfilEmpresa.ADMIN_EMPRESA
                && perfil != PerfilEmpresa.ADMIN_EMPRESA
                && usuarioEmpresaRepository.countAdministradoresAtivosPorEmpresa(empresa) <= 1) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A empresa precisa manter ao menos um administrador");
        }
        vinculo.setPerfil(perfil);
        usuarioEmpresaRepository.save(vinculo);
    }

    @Transactional
    public void reativarAcesso(Long usuarioId, Empresa empresa) {
        UsuarioEmpresa vinculo = usuarioEmpresaRepository.findByUsuarioIdAndEmpresa(usuarioId, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vínculo não encontrado"));
        vinculo.setAtivo(true);
        usuarioEmpresaRepository.save(vinculo);
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
    public void revogarAcesso(Long usuarioId, Empresa empresa) {
        UsuarioEmpresa vinculo = buscarVinculoPorUsuarioEEmpresa(usuarioId, empresa);
        if (vinculo.getPerfil() == PerfilEmpresa.ADMIN_EMPRESA
                && usuarioEmpresaRepository.countAdministradoresAtivosPorEmpresa(empresa) <= 1) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A empresa precisa manter ao menos um administrador");
        }
        vinculo.setAtivo(false);
        usuarioEmpresaRepository.save(vinculo);
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
