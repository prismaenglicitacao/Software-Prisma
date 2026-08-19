package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class EmpresaServiceTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private EmpresaRepository empresaRepository;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setNome("Empresa Teste");
    }

    @Test
    void testCriarEmpresaSemCNPJ() {
        Empresa salva = empresaService.salvar(empresa);
        
        assertNotNull(salva.getId());
        assertEquals("Empresa Teste", salva.getNome());
        assertNull(salva.getCnpj());
        assertTrue(salva.getAtivo());
        assertNotNull(salva.getDataCriacao());
    }

    @Test
    void testCriarEmpresaComCNPJ() {
        empresa.setCnpj("12345678000190");
        
        Empresa salva = empresaService.salvar(empresa);
        
        assertNotNull(salva.getId());
        assertEquals("Empresa Teste", salva.getNome());
        assertEquals("12345678000190", salva.getCnpj());
    }

    @Test
    void testCriarEmpresaComCNPJDuplicado() {
        empresa.setCnpj("12345678000190");
        empresaService.salvar(empresa);
        
        Empresa empresa2 = new Empresa();
        empresa2.setNome("Empresa Teste 2");
        empresa2.setCnpj("12345678000190");
        
        assertThrows(ResponseStatusException.class, () -> empresaService.salvar(empresa2));
    }

    @Test
    void testNomeVazio() {
        empresa.setNome("");
        
        // A validação @NotBlank deve ser testada no controller
        // No service, o nome pode ser vazio se não tiver validação
        // Este teste verifica se o service aceita nome vazio
        Empresa salva = empresaService.salvar(empresa);
        assertEquals("", salva.getNome());
    }

    @Test
    void testAtualizarEmpresaSemCNPJ() {
        Empresa salva = empresaService.salvar(empresa);
        
        Empresa formulario = new Empresa();
        formulario.setNome("Empresa Atualizada");
        
        Empresa atualizada = empresaService.atualizar(salva.getId(), formulario);
        
        assertEquals("Empresa Atualizada", atualizada.getNome());
        assertNull(atualizada.getCnpj());
    }

    @Test
    void testAtualizarEmpresaAdicionandoCNPJ() {
        Empresa salva = empresaService.salvar(empresa);
        
        Empresa formulario = new Empresa();
        formulario.setNome("Empresa Atualizada");
        formulario.setCnpj("98765432000190");
        
        Empresa atualizada = empresaService.atualizar(salva.getId(), formulario);
        
        assertEquals("Empresa Atualizada", atualizada.getNome());
        assertEquals("98765432000190", atualizada.getCnpj());
    }

    @Test
    void testAtualizarEmpresaAlterandoCNPJ() {
        empresa.setCnpj("12345678000190");
        Empresa salva = empresaService.salvar(empresa);
        
        Empresa formulario = new Empresa();
        formulario.setNome("Empresa Atualizada");
        formulario.setCnpj("98765432000190");
        
        Empresa atualizada = empresaService.atualizar(salva.getId(), formulario);
        
        assertEquals("98765432000190", atualizada.getCnpj());
    }

    @Test
    void testAtualizarEmpresaRemovendoCNPJ() {
        empresa.setCnpj("12345678000190");
        Empresa salva = empresaService.salvar(empresa);
        
        Empresa formulario = new Empresa();
        formulario.setNome("Empresa Atualizada");
        formulario.setCnpj(null);
        
        Empresa atualizada = empresaService.atualizar(salva.getId(), formulario);
        
        assertNull(atualizada.getCnpj());
    }

    @Test
    void testAtualizarEmpresaComCNPJDuplicado() {
        empresa.setCnpj("12345678000190");
        empresaService.salvar(empresa);
        
        Empresa empresa2 = new Empresa();
        empresa2.setNome("Empresa Teste 2");
        empresa2.setCnpj("98765432000190");
        Empresa salva2 = empresaService.salvar(empresa2);
        
        Empresa formulario = new Empresa();
        formulario.setNome("Empresa 2 Atualizada");
        formulario.setCnpj("12345678000190");
        
        assertThrows(ResponseStatusException.class, () -> empresaService.atualizar(salva2.getId(), formulario));
    }

    @Test
    void testDesativarEmpresa() {
        Empresa salva = empresaService.salvar(empresa);
        
        empresaService.desativar(salva.getId());
        
        Empresa desativada = empresaService.buscarPorId(salva.getId());
        assertFalse(desativada.getAtivo());
    }

    @Test
    void testAtivarEmpresa() {
        Empresa salva = empresaService.salvar(empresa);
        empresaService.desativar(salva.getId());
        
        empresaService.ativar(salva.getId());
        
        Empresa ativada = empresaService.buscarPorId(salva.getId());
        assertTrue(ativada.getAtivo());
    }
}
