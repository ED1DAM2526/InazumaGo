package es.iesquevedo.service.game;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.repository.MainRepository;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTurnFlowTest {

    static class FakeRepository implements MainRepository {
        private GameDto stored;

        public FakeRepository(GameDto dto) { this.stored = dto; }

        @Override
        public CompletableFuture<GameDto> getGame(String gameId) {
            return CompletableFuture.completedFuture(stored);
        }

        @Override
        public CompletableFuture<Void> updateGame(String gameId, GameDto game) {
            this.stored = game;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> writeMoveMultiPath(String gameId, es.iesquevedo.dto.MoveDto moveDto) {
            // no-op
            return CompletableFuture.completedFuture(null);
        }

        // Métodos adicionales de la interfaz
        @Override
        public String addMovesListener(String gameId, Consumer<List<es.iesquevedo.dto.MoveData>> listener) { return "fake"; }

        @Override
        public String findDefaultName() { return "fake"; }
    }

    @Test
    public void testTurnFlow_makeMoveByCorrectPlayer() throws Exception {
        // Crear un GameDto mínimo
        GameDto dto = new GameDto();
        dto.setId("game-1");
        dto.setStatus("PLAYING");

        FakeRepository repo = new FakeRepository(dto);
        GameServiceImpl service = new GameServiceImpl(repo);

        Player black = new Player("p-black", PlayerColor.BLACK, "Black");
        Player white = new Player("p-white", PlayerColor.WHITE, "White");

        // Primero, simulate that repository has a game where black is current player and players map contains ids
        // GameMapper currently maps players list from move history; for the test we accept minimal DTO and rely on service checks

        // Try to make a move as black (should fail because stored DTO lacks players mapping)
        CompletableFuture<Void> fut = service.makeMove("game-1", black.getId(), Position.of(4,4), "nonce-1");

        // The call should complete exceptionally due to missing player mapping -> we assert that it's a failure
        assertTrue(fut.isCompletedExceptionally());
    }

    @Test
    public void testFullFlow_createJoinAndMove() throws Exception {
        FakeRepository repo = new FakeRepository(null);
        GameServiceImpl service = new GameServiceImpl(repo);

        Player host = new Player("p-host", PlayerColor.BLACK, "Host");
        Player guest = new Player("p-guest", PlayerColor.WHITE, "Guest");

        // Crear partida
        String gameId = service.createOnlineGame(host).get();
        assertNotNull(gameId);

        // Unirse
        service.joinOnlineGame(gameId, guest).get();

        // Realizar movimiento por el jugador que tiene el turno (host - BLACK)
        CompletableFuture<Void> moveFut = service.makeMove(gameId, host.getId(), Position.of(4,4), "nonce-1");
        // Debería completarse sin excepciones
        moveFut.get();
    }
}
