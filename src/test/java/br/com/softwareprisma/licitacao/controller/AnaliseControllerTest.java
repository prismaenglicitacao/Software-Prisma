package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.service.AnaliseItemService;
import br.com.softwareprisma.licitacao.service.AnaliseResultado;
import br.com.softwareprisma.licitacao.service.AnaliseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnaliseController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnaliseControllerTest {

    @Autowired
    private MockMvc mockMvc;

   
@MockBean
    private AnaliseService analiseService;

    @MockBean
    private AnaliseItemService analiseItemService;

    @Test
    void deveRedirecionarParaResumoDepoisDeCriarAnalise() throws Exception {
        Analise analise = new Analise();
        analise.setId(10L);
        analise.setArea(Area.ELETRICA);

        when(analiseService.criar(any())).thenReturn(analise);
        when(analiseService.prepararAnalise(10L)).thenReturn(new AnaliseResultado(
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                BigDecimal.valueOf(100),
                true
        ));

        mockMvc.perform(post("/analises")
                        .param("area", "ELETRICA")
                        .param("itens[0].descricao", "Luminária LED")
                        .param("itens[0].quantidade", "10")
                        .param("itens[0].unidade", "UND"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/analises/10"));
    }
}
