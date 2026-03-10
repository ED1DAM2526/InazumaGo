package es.iesquevedo.repository.inmemory;

import es.iesquevedo.repository.MainRepository;

public class InMemoryMainRepository implements MainRepository {
    @Override
    public String findDefaultName() {
        return "InazumaGoPrevio";
    }
}

