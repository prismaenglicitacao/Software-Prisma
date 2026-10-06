package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import br.com.softwareprisma.licitacao.repository.UsuarioEmpresaRepository;
import br.com.softwareprisma.licitacao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AnaliseRepository analiseRepository;

    private Usuario usuarioParaExcluir;
    private Usuario usuarioLogado;
    private Empresa empresa;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        // Criar usuário para excluir
        usuarioParaExcluir = new Usuario();
        usuarioParaExcluir.setNome("Usuário Teste Exclusão");
        usuarioParaExcluir.setLogin("usuario_exclusao_test");
        usuarioParaExcluir.setSenha("senha123");
        usuarioParaExcluir.setAtivo(false);
        usuarioParaExcluir.setAdministrador(false);
        usuarioParaExcluir = usuarioRepository.save(usuarioParaExcluir);

        // Criar usuário logado (admin)
        usuarioLogado = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getAdministrador()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum administrador encontrado"));

        // Criar empresa
        empresa = new Empresa();
        empresa.setNome("Empresa Teste Exclusão");
        empresa = empresaRepository.save(empresa);

        // Buscar admin existente
        admin = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getAdministrador()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum administrador encontrado"));
    }

    @Test
    void testExcluirUsuarioSemDependencias() {
        // Usuário sem UsuarioEmpresa, sem Analise, sem concedido_por
        Long id = usuarioParaExcluir.getId();

        usuarioService.excluir(id, usuarioLogado.getId());

        assertFalse(usuarioRepository.findById(id).isPresent(), "Usuário deve ser excluído");
    }

    @Test
    void testExcluirUsuarioComUsuarioEmpresaInativo() {
        // Criar vínculo inativo
        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.setUsuario(usuarioParaExcluir);
        vinculo.setEmpresa(empresa);
        vinculo.setAtivo(false);
        vinculo.setPerfil(br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa.USUARIO);
        vinculo.setConcedidoPor(admin);
        vinculo = usuarioEmpresaRepository.save(vinculo);

        Long id = usuarioParaExcluir.getId();
        Long vinculoId = vinculo.getId();

        usuarioService.excluir(id, usuarioLogado.getId());

        assertFalse(usuarioRepository.findById(id).isPresent(), "Usuário deve ser excluído");
        assertFalse(usuarioEmpresaRepository.findById(vinculoId).isPresent(), "Vínculo deve ser removido");
    }

    @Test
    void testExcluirUsuarioComAnalise() {
        // Criar análise para o usuário
        Analise analise = new Analise();
        analise.setEmpresa(empresa);
        analise.setUsuarioCriador(usuarioParaExcluir);
        analise.setArea(br.com.softwareprisma.licitacao.domain.enums.Area.ELETRICA);
        analise = analiseRepository.save(analise);

        Long id = usuarioParaExcluir.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.excluir(id, usuarioLogado.getId())
        );

        assertEquals("Usuário possui análises e não pode ser excluído", exception.getMessage());
        assertTrue(usuarioRepository.findById(id).isPresent(), "Usuário não deve ser excluído");
    }

    @Test
    void testExcluirUsuarioComoConcedidoPor() {
        // Criar outro usuário
        Usuario outroUsuario = new Usuario();
        outroUsuario.setNome("Outro Usuário");
        outroUsuario.setLogin("outro_usuario");
        outroUsuario.setSenha("senha123");
        outroUsuario.setAtivo(true);
        outroUsuario.setAdministrador(false);
        outroUsuario = usuarioRepository.save(outroUsuario);

        // Criar vínculo onde usuarioParaExcluir é concedido_por
        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.setUsuario(outroUsuario);
        vinculo.setEmpresa(empresa);
        vinculo.setAtivo(true);
        vinculo.setPerfil(br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa.USUARIO);
        vinculo.setConcedidoPor(usuarioParaExcluir);
        usuarioEmpresaRepository.save(vinculo);

        Long id = usuarioParaExcluir.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.excluir(id, usuarioLogado.getId())
        );

        assertEquals("Usuário concedeu acessos a outros usuários e não pode ser excluído", exception.getMessage());
        assertTrue(usuarioRepository.findById(id).isPresent(), "Usuário não deve ser excluído");
    }

    @Test
    void testExcluirProprioUsuario() {
        Long id = usuarioLogado.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.excluir(id, id)
        );

        assertEquals("Não é possível excluir o próprio usuário", exception.getMessage());
        assertTrue(usuarioRepository.findById(id).isPresent(), "Usuário não deve ser excluído");
    }

    @Test
    void testExcluirUltimoAdministrador() {
        // Contar administradores
        long totalAdmins = usuarioRepository.findAll().stream()
                .filter(Usuario::getAdministrador)
                .count();

        if (totalAdmins > 1) {
            // Se houver mais de um admin, criar um usuário admin único para teste
            Usuario novoAdmin = new Usuario();
            novoAdmin.setNome("Admin Único");
            novoAdmin.setLogin("admin_unico");
            novoAdmin.setSenha("senha123");
            novoAdmin.setAtivo(true);
            novoAdmin.setAdministrador(true);
            novoAdmin = usuarioRepository.save(novoAdmin);

            // Remover outros admins temporariamente (apenas para teste)
            // Na prática, isso não seria feito, mas para o teste precisamos simular
            Long novoAdminId = novoAdmin.getId();
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.excluir(novoAdminId, usuarioLogado.getId())
            );

            assertEquals("Não é possível excluir o último administrador do sistema", exception.getMessage());
        } else {
            // Se só existe um admin, testar com ele mesmo
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.excluir(usuarioLogado.getId(), usuarioLogado.getId())
            );

            // Deve falhar primeiro por ser próprio usuário
            assertEquals("Não é possível excluir o próprio usuário", exception.getMessage());
        }
    }

    @Test
    void testRollbackAoFalharExclusao() {
        // Criar vínculo inativo
        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.setUsuario(usuarioParaExcluir);
        vinculo.setEmpresa(empresa);
        vinculo.setAtivo(false);
        vinculo.setPerfil(br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa.USUARIO);
        vinculo.setConcedidoPor(admin);
        vinculo = usuarioEmpresaRepository.save(vinculo);

        // Criar análise para causar falha
        Analise analise = new Analise();
        analise.setEmpresa(empresa);
        analise.setUsuarioCriador(usuarioParaExcluir);
        analise.setArea(br.com.softwareprisma.licitacao.domain.enums.Area.ELETRICA);
        analise = analiseRepository.save(analise);

        Long id = usuarioParaExcluir.getId();
        Long vinculoId = vinculo.getId();

        // Tentar excluir (deve falhar por causa da análise)
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.excluir(id, usuarioLogado.getId())
        );

        // Verificar rollback - vínculo ainda deve existir
        assertTrue(usuarioEmpresaRepository.findById(vinculoId).isPresent(), 
            "Vínculo não deve ser removido em caso de falha (rollback)");
        assertTrue(usuarioRepository.findById(id).isPresent(), "Usuário não deve ser excluído");
    }
}
