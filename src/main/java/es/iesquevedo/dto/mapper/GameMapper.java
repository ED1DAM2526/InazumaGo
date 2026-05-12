package es.iesquevedo.dto.mapper;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.model.game.Game;
import es.iesquevedo.model.game.GameState;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.player.PlayerColor;

import java.util.stream.Collectors;

/**
 * Mapper entre Game (dominio) y GameDto (persistencia)
 */
public class GameMapper {
    
    public static GameDto toDto(Game game) {
        GameDto dto = new GameDto();
        dto.setId(game.getGameId());
        dto.setName("Game-" + game.getGameId().substring(0, 8));
        dto.setStatus(game.getState().toString());
        dto.setPlayers(game.getMoveHistory().stream()
            .map(m -> m.getActor().name())
            .distinct()
            .collect(Collectors.toList())
        );
        dto.setCreatedAt(System.currentTimeMillis());
        
        return dto;
    }
    
    public static Game toEntity(GameDto dto) {
        Game game = new Game(dto.getId());
        game.setState(GameState.valueOf(dto.getStatus()));
        
        return game;
    }
}

