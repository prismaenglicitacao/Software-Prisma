package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalSearchServiceTest {

    @Mock
    private EngenheiroRepository engenheiroRepository;

    @Mock
    private CatRepository catRepository;

    @Mock
    private CatItemRepository catItemRepository;

    @InjectMocks
    private GlobalSearchService globalSearchService;

    @Test
    void pesquisar_SemEmpresa_DeveRejeitarSemConsultarRepositories() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> globalSearchService.pesquisar("termo", null));

        assertEquals(403, exception.getStatusCode().value());
        verifyNoInteractions(engenheiroRepository, catRepository, catItemRepository);
    }

    @Test
    void pesquisar_DeveUsarSomenteQueriesDaEmpresaAtiva() {
        Empresa empresaA = new Empresa();
        empresaA.setId(1L);
        empresaA.setNome("Empresa A");
        Empresa empresaB = new Empresa();
        empresaB.setId(2L);
        empresaB.setNome("Empresa B");

        when(engenheiroRepository.pesquisarPorNomeEEmpresa(eq("obra"), eq(empresaA), any()))
                .thenReturn(List.of());
        when(catRepository.pesquisarPorNomeOuNumeroEEmpresa(eq("obra"), eq(empresaA), any()))
                .thenReturn(List.of());
        when(catItemRepository.pesquisarPorDescricaoEEmpresa(eq("obra"), eq(empresaA), any()))
                .thenReturn(List.of());
        when(engenheiroRepository.pesquisarPorNomeEEmpresa(eq("obra"), eq(empresaB), any()))
                .thenReturn(List.of());
        when(catRepository.pesquisarPorNomeOuNumeroEEmpresa(eq("obra"), eq(empresaB), any()))
                .thenReturn(List.of());
        when(catItemRepository.pesquisarPorDescricaoEEmpresa(eq("obra"), eq(empresaB), any()))
                .thenReturn(List.of());

        globalSearchService.pesquisar("obra", empresaA);
        globalSearchService.pesquisar("obra", empresaB);

        verify(engenheiroRepository).pesquisarPorNomeEEmpresa(eq("obra"), eq(empresaA), any());
        verify(catRepository).pesquisarPorNomeOuNumeroEEmpresa(eq("obra"), eq(empresaA), any());
        verify(catItemRepository).pesquisarPorDescricaoEEmpresa(eq("obra"), eq(empresaA), any());
        verify(engenheiroRepository).pesquisarPorNomeEEmpresa(eq("obra"), eq(empresaB), any());
        verify(catRepository).pesquisarPorNomeOuNumeroEEmpresa(eq("obra"), eq(empresaB), any());
        verify(catItemRepository).pesquisarPorDescricaoEEmpresa(eq("obra"), eq(empresaB), any());
        verify(engenheiroRepository, never()).pesquisarPorNome(anyString(), any());
        verify(catRepository, never()).pesquisarPorNomeOuNumero(anyString(), any());
        verify(catItemRepository, never()).pesquisarPorDescricao(anyString(), any());
    }
}
