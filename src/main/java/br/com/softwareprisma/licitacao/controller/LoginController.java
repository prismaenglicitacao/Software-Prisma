package br.com.softwareprisma.licitacao.controller;

import jakarta.servlet.http.HttpSession;
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
                       @RequestParam(value = "locked", required = false) String locked,
                       HttpSession session,
                       Model model) {
        if (locked != null) {
            model.addAttribute("error", "Muitas tentativas de login. Tente novamente mais tarde.");
        }
        if (error != null) {
            String lockError = (String) session.getAttribute("lockError");
            if (lockError != null) {
                model.addAttribute("error", lockError);
                session.removeAttribute("lockError");
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
