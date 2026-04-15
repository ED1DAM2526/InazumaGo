package es.iesquevedo.service.impl;

import es.iesquevedo.exception.NotFoundException;
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
        if (name == null || name.isBlank()) {
            throw new NotFoundException("Default player name not found");
        }
        return "Hello, " + name + "!";
    }
}
