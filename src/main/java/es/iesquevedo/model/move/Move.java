package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.player.PlayerColor;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa un movimiento en la partida
 */
public class Move {
    private final String moveId;
    private final Position position;
    private final PlayerColor actor;
    private final long clientTimestamp;
    private final String clientNonce; // Para deduplicación

    /**
     * Constructor para movimiento en una posición
     */
    public Move(Position position, PlayerColor actor, String clientNonce) {
        this.moveId = UUID.randomUUID().toString();
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.actor = Objects.requireNonNull(actor, "Actor cannot be null");
        this.clientNonce = Objects.requireNonNull(clientNonce, "Client nonce cannot be null");
        this.clientTimestamp = System.currentTimeMillis();
    }

    /**
     * Constructor privado para reconstrucción con ID
     */
    private Move(String moveId, Position position, PlayerColor actor, 
                 long clientTimestamp, String clientNonce) {
        this.moveId = moveId;
        this.position = position;
        this.actor = actor;
        this.clientTimestamp = clientTimestamp;
        this.clientNonce = clientNonce;
    }

    public String getMoveId() {
        return moveId;
    }

    public Position getPosition() {
        return position;
    }

    public PlayerColor getActor() {
        return actor;
    }

    public long getClientTimestamp() {
        return clientTimestamp;
    }

    public String getClientNonce() {
        return clientNonce;
    }

    /**
     * Crear move desde datos conocidos (para desserialización)
     */
    public static Move of(String moveId, Position position, PlayerColor actor, 
                         long clientTimestamp, String clientNonce) {
        return new Move(moveId, position, actor, clientTimestamp, clientNonce);
    }

    /**
     * Movimiento de pase (sin posición)
     */
    public static Move pass(PlayerColor actor, String clientNonce) {
        Move move = new Move(null, actor, clientNonce);
        return move;
    }

    public boolean isPass() {
        return position == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return moveId.equals(move.moveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moveId);
    }

    @Override
    public String toString() {
        String posStr = isPass() ? "PASS" : position.toGoNotation();
        return String.format("Move{id=%s, pos=%s, actor=%s, nonce=%s}", 
            moveId, posStr, actor, clientNonce.substring(0, 8) + "...");
    }
}

