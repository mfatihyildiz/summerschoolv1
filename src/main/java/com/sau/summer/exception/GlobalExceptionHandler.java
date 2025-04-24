package com.sau.summer.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 Not Found (URL yanlış girildiğinde)
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Sayfa bulunamadı!");
        return "error-page";
    }

    // Tüm Exception'ları yakalar (NullPointer, DB hatası, vs.)
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Beklenmeyen bir hata oluştu!");
        return "error-page";
    }
}
