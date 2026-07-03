package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO;
import br.com.softwareprisma.licitacao.controller.dto.EstatisticasGeraisDTO;
import br.com.softwareprisma.licitacao.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        EstatisticasGeraisDTO estatisticas = dashboardService.getEstatisticasGerais();
        Page<AnaliseResumoDTO> ultimasAnalises = dashboardService.getUltimasAnalises(page);
        Map<String, Long> catsPorArea = dashboardService.getCatsPorArea();
        Map<String, Long> itensPorArea = dashboardService.getItensPorArea();

        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("ultimasAnalises", ultimasAnalises.getContent());
        model.addAttribute("pagina", ultimasAnalises);
        model.addAttribute("catsPorArea", catsPorArea);
        model.addAttribute("itensPorArea", itensPorArea);

        return "dashboard/index";
    }
}
