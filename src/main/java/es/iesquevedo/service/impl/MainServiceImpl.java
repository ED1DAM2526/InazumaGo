package es.iesquevedo.service.impl;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.MainService;
import java.util.Objects;

public class MainServiceImpl implements MainService {
    private final MainRepository repository;

    public MainServiceImpl(MainRepository repository) {
        this.repository = repository;
    }

    @Override
    public String greet() {
        String name = repository.findDefaultName();
        // Evita NPE si el repositorio devuelve null
        return "Hello, " + Objects.toString(name, "player") + "!";
    }
}
