package es.iesquevedo.service.impl;

import es.iesquevedo.exception.NotFoundException;
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
        if (name == null) {
            throw new NotFoundException("Default name not found in repository");
        }
        // Evita NPE si el repositorio devuelve null
        return "Hello, " + Objects.toString(name, "player") + "!";
    }
}
