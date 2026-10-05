package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.GlobalSearchDTO;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import br.com.softwareprisma.licitacao.service.GlobalSearchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalSearchControllerTest {

    private EmpresaAtivaService empresaAtivaService;
    private GlobalSearchService globalSearchService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        empresaAtivaService = mock(EmpresaAtivaService.class);
        globalSearchService = mock(GlobalSearchService.class);
        GlobalSearchController controller = new GlobalSearchController(globalSearchService, empresaAtivaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        Usuario usuario = new Usuario();
        usuario.setLogin("usuario-teste");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, "n/a", usuario.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pesquisar_SemEmpresaAtiva_DeveResponder403SemChamarBusca() throws Exception {
        when(empresaAtivaService.obterEmpresaAtivaObrigatoria(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado."));

        mockMvc.perform(get("/api/pesquisa").param("q", "obra"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(globalSearchService);
    }

    @Test
    void pesquisar_IgnoraEmpresaIdDoClienteEUsaEmpresaAtivaDaSessao() throws Exception {
        Empresa empresaAtiva = new Empresa();
        empresaAtiva.setId(1L);
        when(empresaAtivaService.obterEmpresaAtivaObrigatoria(any(), any())).thenReturn(empresaAtiva);
        when(globalSearchService.pesquisar("obra", empresaAtiva))
                .thenReturn(new GlobalSearchDTO(List.of(), List.of(), List.of(), false));

        mockMvc.perform(get("/api/pesquisa")
                        .param("q", "obra")
                        .param("empresaId", "999"))
                .andExpect(status().isOk());

        verify(globalSearchService).pesquisar("obra", empresaAtiva);
        verify(empresaAtivaService).obterEmpresaAtivaObrigatoria(any(), any());
    }
}
