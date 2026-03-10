package es.iesquevedo.controller;

import es.iesquevedo.service.MainService;

public class MainController {
    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    public String greet() {
        return mainService.greet();
    }
}

