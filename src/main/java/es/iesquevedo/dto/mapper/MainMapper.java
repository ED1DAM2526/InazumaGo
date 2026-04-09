package es.iesquevedo.dto.mapper;

import es.iesquevedo.dto.MainDto;
import es.iesquevedo.model.MainEntity;

public final class MainMapper {
    private MainMapper() {
        // Constructor privado para evitar instanciación
    }

    public static MainDto toDto(MainEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MainDto(entity.getId(), entity.getName());
    }
}

