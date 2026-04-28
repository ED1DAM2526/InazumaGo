package es.iesquevedo.dto;

public class MainDto {
    private final long id;
    private final String name;

    public MainDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
