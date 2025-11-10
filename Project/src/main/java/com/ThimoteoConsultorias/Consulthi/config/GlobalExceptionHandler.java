package com.ThimoteoConsultorias.Consulthi.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler
{
    // Captura de Erros de Validação (400 BAD REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleBadRequest(IllegalArgumentException e)
    {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorName", "Requisição Inválida (400)");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.BAD_REQUEST);
        return mav;
    }
    
    // Captura de Erros de Segurança (Ex: SecurityException - 403 FORBIDDEN)
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden()
    {
        return "redirect:/login?accessDenied"; 
    }

    // Captura de Exceções Genéricas (500 INTERNAL SERVER ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception e)
    {
        ModelAndView mav = new ModelAndView("error"); 
        mav.addObject("errorName", "Erro Interno do Servidor (500)");
        mav.addObject("errorMessage", "Ocorreu um erro inesperado: " + e.getMessage());
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}