package es.iesquevedo.dto.mapper;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.model.game.Game;
import es.iesquevedo.model.game.GameState;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;

import java.util.ArrayList;
import java.util.List;
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
        // Mapear jugadores por color si existen
        List<String> players = new ArrayList<>();
        try {
            Player black = game.getPlayer(PlayerColor.BLACK);
            Player white = game.getPlayer(PlayerColor.WHITE);
            if (black != null) players.add(black.getId());
            if (white != null) players.add(white.getId());
        } catch (Exception e) {
            // ignore
        }
        dto.setPlayers(players);
        dto.setCreatedAt(System.currentTimeMillis());
        dto.setGameVersion(String.valueOf(System.currentTimeMillis()));

        return dto;
    }
    
    public static Game toEntity(GameDto dto) {
        Game game = new Game(dto.getId());
        try {
            game.setState(GameState.valueOf(dto.getStatus()));
        } catch (Exception e) {
            game.setState(GameState.WAITING);
        }

        // Reconstruir jugadores si vienen en dto.players (se asume orden: BLACK, WHITE)
        List<String> players = dto.getPlayers();
        if (players != null && !players.isEmpty()) {
            if (players.size() >= 1 && players.get(0) != null) {
                game.addPlayer(new Player(players.get(0), PlayerColor.BLACK, players.get(0)));
            }
            if (players.size() >= 2 && players.get(1) != null) {
                game.addPlayer(new Player(players.get(1), PlayerColor.WHITE, players.get(1)));
            }
        }

        return game;
    }
}
