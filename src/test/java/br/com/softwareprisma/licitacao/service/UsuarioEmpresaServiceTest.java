package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import br.com.softwareprisma.licitacao.repository.UsuarioEmpresaRepository;
import br.com.softwareprisma.licitacao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UsuarioEmpresaServiceTest {

    @Autowired
    private UsuarioEmpresaService usuarioEmpresaService;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    private Usuario usuario;
    private Empresa empresa;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(usuario, "Nenhum usuário encontrado no banco de dados");

        empresa = new Empresa();
        empresa.setNome("Empresa Teste");
        empresa = empresaRepository.save(empresa);

        admin = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getAdministrador()))
                .findFirst()
                .orElse(usuario);
    }

    @Test
    void testConcederAcessoUsuarioSemVinculo() {
        UsuarioEmpresa vinculo = usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        assertNotNull(vinculo.getId());
        assertEquals(usuario, vinculo.getUsuario());
        assertEquals(empresa, vinculo.getEmpresa());
        assertTrue(vinculo.getAtivo());
        assertEquals(admin, vinculo.getConcedidoPor());
        assertNotNull(vinculo.getDataConcessao());
    }

    @Test
    void testConcederAcessoJaAtivo() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        assertThrows(ResponseStatusException.class, () -> 
            usuarioEmpresaService.concederAcesso(usuario, empresa, admin)
        );
    }

    @Test
    void testConcederAcessoAposRevogarReativacao() {
        // Conceder acesso
        UsuarioEmpresa vinculo = usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        Long idOriginal = vinculo.getId();
        
        // Revogar acesso
        usuarioEmpresaService.revogarAcesso(usuario, empresa);
        
        UsuarioEmpresa revogado = usuarioEmpresaRepository.findById(idOriginal).orElse(null);
        assertNotNull(revogado);
        assertFalse(revogado.getAtivo());
        
        // Conceder novamente (deve reativar, não criar novo)
        UsuarioEmpresa reativado = usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        assertEquals(idOriginal, reativado.getId(), "Deve reativar o mesmo registro");
        assertTrue(reativado.getAtivo());
    }

    @Test
    void testRevogarAcesso() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        usuarioEmpresaService.revogarAcesso(usuario, empresa);
        
        assertFalse(usuarioEmpresaService.usuarioTemAcesso(usuario, empresa));
    }

    @Test
    void testRevogarAcessoInexistente() {
        assertThrows(ResponseStatusException.class, () -> 
            usuarioEmpresaService.revogarAcesso(usuario, empresa)
        );
    }

    @Test
    void testReativarAcesso() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        usuarioEmpresaService.revogarAcesso(usuario, empresa);
        
        usuarioEmpresaService.reativarAcesso(usuario, empresa);
        
        assertTrue(usuarioEmpresaService.usuarioTemAcesso(usuario, empresa));
    }

    @Test
    void testReativarAcessoInexistente() {
        assertThrows(ResponseStatusException.class, () -> 
            usuarioEmpresaService.reativarAcesso(usuario, empresa)
        );
    }

    @Test
    void testRemoverVinculo() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        usuarioEmpresaService.removerVinculo(usuario, empresa);
        
        assertFalse(usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario, empresa).isPresent());
    }

    @Test
    void testRemoverVinculoInexistente() {
        assertThrows(ResponseStatusException.class, () -> 
            usuarioEmpresaService.removerVinculo(usuario, empresa)
        );
    }

    @Test
    void testUsuarioTemAcesso() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        assertTrue(usuarioEmpresaService.usuarioTemAcesso(usuario, empresa));
    }

    @Test
    void testUsuarioNaoTemAcesso() {
        assertFalse(usuarioEmpresaService.usuarioTemAcesso(usuario, empresa));
    }

    @Test
    void testListarPorUsuario() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        var empresas = usuarioEmpresaService.listarPorUsuario(usuario);
        
        assertFalse(empresas.isEmpty());
        assertEquals(empresa.getId(), empresas.get(0).getEmpresa().getId());
    }

    @Test
    void testListarPorEmpresa() {
        usuarioEmpresaService.concederAcesso(usuario, empresa, admin);
        
        var usuarios = usuarioEmpresaService.listarPorEmpresa(empresa);
        
        assertFalse(usuarios.isEmpty());
        assertEquals(usuario.getId(), usuarios.get(0).getUsuario().getId());
    }
}
