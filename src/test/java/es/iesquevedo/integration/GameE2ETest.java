package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.GameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import org.junit.jupiter.api.*;

        import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class GameE2ETest {

    private GameService gameService;
    private MainRepository repository;

    @BeforeEach
    void setUp() {
        // Use in-memory repository for fast tests (no Firebase needed)
        repository = new InMemoryMainRepository();
        gameService = new GameServiceImpl(repository);
    }

    @Test
    void testFullGameHappyPath() throws Exception {
        String player1 = "player1";
        String player2 = "player2";

        // Player 1 creates game
        String gameId = gameService.createGame(player1).get(5, TimeUnit.SECONDS);
        assertNotNull(gameId);

        // Player 2 joins game
        GameStateDto afterJoin = gameService.joinGame(gameId, player2).get(5, TimeUnit.SECONDS);
        assertNotNull(afterJoin);
        assertEquals("IN_PROGRESS", afterJoin.getStatus());
        assertEquals(2, afterJoin.getPlayers().size());

        // Player 1 makes a move
        MovePayload payload = new MovePayload(0, 0, player1);
        GameStateDto afterMove = gameService.submitMove(gameId, player1, payload).get(5, TimeUnit.SECONDS);
        assertNotNull(afterMove);
    }

    @Test
    void testMoveRejectedWrongTurn() throws Exception {
        String player1 = "player1";
        String player2 = "player2";

        // Create and join game
        String gameId = gameService.createGame(player1).get(5, TimeUnit.SECONDS);
        gameService.joinGame(gameId, player2).get(5, TimeUnit.SECONDS);

        // Player 2 tries to move when it's player1's turn - should be rejected
        MovePayload payload = new MovePayload(0, 0, player2);
        CompletableFuture<GameStateDto> future = gameService.submitMove(gameId, player2, payload);

        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    void testMoveRejectedInvalidCell() throws Exception {
        String player1 = "player1";
        String player2 = "player2";

        // Create and join game
        String gameId = gameService.createGame(player1).get(5, TimeUnit.SECONDS);
        gameService.joinGame(gameId, player2).get(5, TimeUnit.SECONDS);

        // Player 1 makes a valid move
        MovePayload firstMove = new MovePayload(0, 0, player1);
        gameService.submitMove(gameId, player1, firstMove).get(5, TimeUnit.SECONDS);

        // Player 2 tries to move to the same cell - should be rejected
        MovePayload invalidMove = new MovePayload(0, 0, player2);
        CompletableFuture<GameStateDto> future = gameService.submitMove(gameId, player2, invalidMove);

        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    void testGetGameState() throws Exception {
        String player1 = "player1";

        // Create game
        String gameId = gameService.createGame(player1).get(5, TimeUnit.SECONDS);

        // Get game state
        GameStateDto state = gameService.getGameState(gameId).get(5, TimeUnit.SECONDS);

        assertNotNull(state);
        assertEquals(gameId, state.getGameId());
        assertEquals("WAITING", state.getStatus());
        assertEquals(1, state.getPlayers().size());
        assertEquals(player1, state.getCurrentTurnPlayerId());
    }
}
