package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO;
import br.com.softwareprisma.licitacao.controller.dto.EstatisticasGeraisDTO;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.DashboardService;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping
    public String dashboard(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            HttpSession session,
                            @AuthenticationPrincipal Usuario usuario) {
        if (Boolean.TRUE.equals(usuario.getAdministrador())) {
            return "redirect:/admin";
        }

        Empresa empresaAtiva = empresaAtivaService.getEmpresaAtiva(session, usuario);
        if (empresaAtiva == null) {
            return "redirect:/escolher-empresa";
        }

        EstatisticasGeraisDTO estatisticas = dashboardService.getEstatisticasGerais(empresaAtiva);
        Page<AnaliseResumoDTO> ultimasAnalises = dashboardService.getUltimasAnalises(page, empresaAtiva);
        Map<String, Long> catsPorArea = dashboardService.getCatsPorArea(empresaAtiva);
        Map<String, Long> itensPorArea = dashboardService.getItensPorArea(empresaAtiva);

        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("ultimasAnalises", ultimasAnalises.getContent());
        model.addAttribute("pagina", ultimasAnalises);
        model.addAttribute("catsPorArea", catsPorArea);
        model.addAttribute("itensPorArea", itensPorArea);

        return "dashboard/index";
    }
}
