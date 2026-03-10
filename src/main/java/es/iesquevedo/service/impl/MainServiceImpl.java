package es.iesquevedo.service.impl;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.MainService;

public class MainServiceImpl implements MainService {
    private final MainRepository repository;

    public MainServiceImpl(MainRepository repository) {
        this.repository = repository;
    }

    @Override
    public String greet() {
        String name = repository.findDefaultName();
        return "Hello, " + name + "!";
    }
}

