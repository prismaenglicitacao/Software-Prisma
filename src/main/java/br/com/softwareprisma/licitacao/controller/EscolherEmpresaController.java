package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.service.EmpresaAtivaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EscolherEmpresaController {

    private final EmpresaAtivaService empresaAtivaService;

    @GetMapping("/escolher-empresa")
    public String escolherEmpresa(@RequestParam(value = "sem_acesso", required = false) String semAcesso,
                                  @AuthenticationPrincipal Usuario usuario,
                                  Model model) {
        if (semAcesso != null) {
            model.addAttribute("semAcesso", true);
            return "escolher-empresa";
        }

        List<Empresa> empresas = empresaAtivaService.listarEmpresasDoUsuario(usuario);
        model.addAttribute("empresas", empresas);
        return "escolher-empresa";
    }
}
