package es.iesquevedo.repository;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Contrato principal de acceso a datos para la app.
 * Define operaciones de lectura/escritura de juego y datos auxiliares.
 */
public interface MainRepository {
	CompletableFuture<GameDto> getGame(String gameId);

	CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload);

	String addMovesListener(String gameId, Consumer<List<MoveData>> listener);

	String findDefaultName();
}

