package es.iesquevedo.dto.mapper;

import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.Position;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.player.PlayerColor;

/**
 * Mapper entre Move (dominio) y MoveDto (persistencia)
 */
public class MoveMapper {
    
    public static MoveDto toDto(Move move) {
        MoveDto dto = new MoveDto();
        dto.setTimestamp(move.getClientTimestamp());
        dto.setGameVersion(move.getMoveId());
        
        if (!move.isPass()) {
            MoveData moveData = new MoveData(
                move.getActor().name(),
                "MOVE",
                new Position(move.getPosition().getRow(), move.getPosition().getCol())
            );
            dto.setMoves(java.util.List.of(moveData));
        }
        
        return dto;
    }
    
    public static Move toEntity(MoveDto dto, PlayerColor actor, String clientNonce) {
        if (dto.getMoves() == null || dto.getMoves().isEmpty()) {
            return Move.pass(actor, clientNonce);
        }
        
        MoveData moveData = dto.getMoves().get(0);
        Position pos = moveData.getPosition();
        es.iesquevedo.model.board.Position position = 
            es.iesquevedo.model.board.Position.of(pos.getX(), pos.getY());
        
        return Move.of(
            dto.getGameVersion(),
            position,
            actor,
            dto.getTimestamp(),
            clientNonce
        );
    }
}

