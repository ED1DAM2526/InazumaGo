package es.iesquevedo.integration.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Configurador de stubs de WireMock para eventos de juego.
 * Proporciona métodos de utilidad para configurar stubs HTTP en tests de integración.
 */
public class GameEventWireMockStubs {

    /**
     * Configura el stub para evento de inicio de partida
     */
    public static void stubGameStart(String gameId) {
        stubFor(post(urlEqualTo("/api/events/game.start"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildEventResponse("game.start", gameId))));
    }

    /**
     * Configura el stub para evento de movimiento
     */
    public static void stubGameMove(String gameId) {
        stubFor(post(urlEqualTo("/api/events/game.move"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildEventResponse("game.move", gameId))));
    }

    /**
     * Configura el stub para evento de fin de partida
     */
    public static void stubGameEnd(String gameId) {
        stubFor(post(urlEqualTo("/api/events/game.end"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildEventResponse("game.end", gameId))));
    }

    /**
     * Configura el stub para todos los eventos
     */
    public static void stubAllGameEvents(String gameId) {
        stubGameStart(gameId);
        stubGameMove(gameId);
        stubGameEnd(gameId);
    }

    /**
     * Configura el stub para responder con error
     */
    public static void stubGameEventError(String eventType, int statusCode, String errorMessage) {
        stubFor(post(urlEqualTo("/api/events/" + eventType))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"" + errorMessage + "\"}")));
    }

    /**
     * Verifica que se haya realizado una solicitud POST a un endpoint de evento
     */
    public static void verifyEventRequest(String eventType) {
        verify(postRequestedFor(urlEqualTo("/api/events/" + eventType)));
    }

    /**
     * Verifica que se haya realizado una solicitud POST con un número específico de intentos
     */
    public static void verifyEventRequest(String eventType, int times) {
        verify(times(times), postRequestedFor(urlEqualTo("/api/events/" + eventType)));
    }

    /**
     * Construye la respuesta JSON para un evento
     */
    private static String buildEventResponse(String eventType, String gameId) {
        return "{" +
            "\"id\": \"event_" + System.currentTimeMillis() + "\"," +
            "\"type\": \"" + eventType + "\"," +
            "\"gameId\": \"" + gameId + "\"," +
            "\"timestamp\": " + System.currentTimeMillis() +
            "}";
    }

}

