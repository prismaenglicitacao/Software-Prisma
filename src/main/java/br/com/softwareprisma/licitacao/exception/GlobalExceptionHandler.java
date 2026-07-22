package br.com.softwareprisma.licitacao.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("Erro não tratado: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Ocorreu um erro inesperado. Por favor, tente novamente.");
        return "error";
    }
}
