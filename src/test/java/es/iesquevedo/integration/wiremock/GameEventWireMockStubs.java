package es.iesquevedo.integration.wiremock;

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
            .withRequestBody(containing("\"gameId\":\"" + gameId + "\""))
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
            .withRequestBody(containing("\"gameId\":\"" + gameId + "\""))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildEventResponse("game.move", gameId))));
    }

    /**
     * Configura el stub para un movimiento rechazado por reglas de turno.
     */
    public static void stubGameMoveForbidden(String gameId) {
        stubFor(post(urlEqualTo("/api/events/game.move"))
            .withRequestBody(containing("\"gameId\":\"" + gameId + "\""))
            .willReturn(aResponse()
                .withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Forbidden\"}")));
    }

    /**
     * Configura el stub para evento de fin de partida
     */
    public static void stubGameEnd(String gameId) {
        stubFor(post(urlEqualTo("/api/events/game.end"))
            .withRequestBody(containing("\"gameId\":\"" + gameId + "\""))
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
     * Configura un stub de recuperación de eventos para una partida concreta.
     */
    public static void stubRecoveredGameEvents(String gameId, String responseJson) {
        stubFor(get(urlPathEqualTo("/api/events"))
            .withQueryParam("gameId", equalTo(gameId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseJson)));
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
        verify(times, postRequestedFor(urlEqualTo("/api/events/" + eventType)));
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

