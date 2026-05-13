package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.player.PlayerColor;

import java.util.Objects;

/**
 * Representa un movimiento en la partida
 */
public class Move {
    private final Position position;
    private final PlayerColor actor;
    private final String clientNonce;
    private final boolean pass;

    /**
     * Constructor para movimiento en una posición
     */
    public Move(Position position, PlayerColor actor, String clientNonce) {
        this.position = position; // allow null if pass
        this.actor = Objects.requireNonNull(actor);
        this.clientNonce = clientNonce;
        this.pass = false;
        // Not enforcing non-null position here to allow pass construction via static method
    }

    /**
     * Constructor privado para reconstrucción con ID
     */
    private Move(boolean pass, Position position, PlayerColor actor, String clientNonce) {
        this.position = position;
        this.actor = actor;
        this.clientNonce = clientNonce;
        this.pass = pass;
    }

    /**
     * Movimiento de pase (sin posición)
     */
    public static Move pass(PlayerColor actor, String clientNonce) {
        return new Move(true, null, Objects.requireNonNull(actor), clientNonce);
    }

    public Position getPosition() {
        return position;
    }

    public PlayerColor getActor() {
        return actor;
    }

    public String getClientNonce() {
        return clientNonce;
    }

    public boolean isPass() {
        return pass;
    }

    @Override
    public String toString() {
        return "Move{" + (pass ? "PASS" : position) + ", actor=" + actor + '}';
    }
}
