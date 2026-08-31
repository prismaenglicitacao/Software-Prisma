package br.com.softwareprisma.licitacao.controller;

import br.com.softwareprisma.licitacao.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SelecionarEmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/selecionar-empresa")
    public String selecionarEmpresa(Model model) {
        model.addAttribute("empresas", empresaService.listarAtivas());
        return "selecionar-empresa";
    }
}
