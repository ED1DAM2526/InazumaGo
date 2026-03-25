package es.iesquevedo.controller;

import es.iesquevedo.exception.ApiError;
import es.iesquevedo.exception.GlobalExceptionHandler;
import es.iesquevedo.service.MainService;

/**
 * Controlador principal que orquesta llamadas al servicio y maneja errores.
 * 
 * Responsabilidades:
 * - Delegar en MainService
 * - Capturar excepciones y mapearlas a ApiError mediante GlobalExceptionHandler
 * - Retornar respuestas estandarizadas a la UI
 */
public class MainController {
    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    /**
     * Método que delega al servicio y devuelve un saludo.
     * 
     * @return saludo del servicio
     */
    public String greet() {
        return mainService.greet();
    }

    /**
     * Método que delega al servicio con manejo seguro de excepciones.
     * Usa GlobalExceptionHandler para transformar excepciones a ApiError.
     * 
     * @return saludo del servicio o ApiError en caso de excepción
     */
    public Object greetSafe() {
        try {
            return mainService.greet();
        } catch (Exception e) {
            return GlobalExceptionHandler.handle(e);
        }
    }
}

