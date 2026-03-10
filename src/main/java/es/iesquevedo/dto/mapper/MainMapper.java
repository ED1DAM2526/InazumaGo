package es.iesquevedo.dto.mapper;

import es.iesquevedo.dto.MainDto;
import es.iesquevedo.model.MainEntity;

public class MainMapper {
    public static MainDto toDto(MainEntity entity) {
        if (entity == null) return null;
        return new MainDto(entity.getId(), entity.getName());
    }
}

