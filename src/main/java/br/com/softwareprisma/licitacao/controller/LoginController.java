package br.com.softwareprisma.licitacao.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "expired", required = false) String expired,
                       Model model) {
        if (error != null) {
            String lockError = (String) model.getAttribute("lockError");
            if (lockError != null) {
                model.addAttribute("error", lockError);
            } else {
                model.addAttribute("error", "Usuário ou senha inválidos");
            }
        }
        if (logout != null) {
            model.addAttribute("message", "Você saiu do sistema com sucesso");
        }
        if (expired != null) {
            model.addAttribute("message", "Sua sessão expirou. Faça login novamente.");
        }
        return "login";
    }
}
