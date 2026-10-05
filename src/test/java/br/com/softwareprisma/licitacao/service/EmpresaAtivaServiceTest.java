package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import br.com.softwareprisma.licitacao.repository.UsuarioEmpresaRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaAtivaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private EmpresaAtivaService empresaAtivaService;

    @Test
    void obterEmpresaAtivaObrigatoria_SemEmpresaAtiva_DeveRejeitarCom403() {
        Usuario usuario = new Usuario();
        when(session.getAttribute("empresaAtivaId")).thenReturn(null);
        when(usuarioEmpresaRepository.findByUsuarioAtivoTrueComEmpresa(usuario)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> empresaAtivaService.obterEmpresaAtivaObrigatoria(session, usuario));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Acesso negado.", exception.getReason());
        verifyNoInteractions(empresaRepository);
    }

    @Test
    void obterEmpresaAtivaObrigatoria_DeveRetornarEmpresaAutorizadaDaSessao() {
        Usuario usuario = new Usuario();
        Empresa empresa = new Empresa();
        empresa.setId(42L);
        empresa.setAtivo(true);

        when(session.getAttribute("empresaAtivaId")).thenReturn(42L);
        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresaIdAndAtivoTrue(usuario, 42L)).thenReturn(true);
        when(empresaRepository.findById(42L)).thenReturn(java.util.Optional.of(empresa));

        assertSame(empresa, empresaAtivaService.obterEmpresaAtivaObrigatoria(session, usuario));
    }
}
