package com.project;

import jakarta.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebsocketClient {

    private static WebsocketClient instance;
    private Session session;
    private WaitingViewController waitingController;
    private String myPlayerId;

    // Patrón Singleton para poder acceder a la red desde cualquier pantalla fácilmente
    public static WebsocketClient getInstance() {
        if (instance == null) {
            instance = new WebsocketClient();
        }
        return instance;
    }

    // Permite a la sala de espera registrarse para escuchar los datos del servidor
    public void setWaitingController(WaitingViewController controller) {
        this.waitingController = controller;
    }

    /**
     * Abre la conexión WebSocket con el servidor de forma asíncrona
     */
    public void connect(String serverUrl) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            System.out.println("[Red] Intentando conectar a: " + serverUrl);
            container.connectToServer(this, URI.create(serverUrl));
        } catch (Exception e) {
            System.err.println("[Red] Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.myPlayerId = session.getId(); // El servidor usa la ID de la sesión de WebSocket
        System.out.println("[Red] Conectado. Mi ID de jugador es: " + myPlayerId);
    }

    // Añade este getter abajo para que el juego pueda consultarlo
    public String getMyPlayerId() {
        return myPlayerId;
    }

    @OnMessage
    public void onMessage(String message) {
        // Aquí llega el JSON de tipo "STATE" 30 veces por segundo.
        // Contamos cuántos tanques hay en la partida contando las apariciones de '"id":'
        int totalPlayers = countOccurrences(message, "\"id\":");

        // Si estamos en la sala de espera, le enviamos el número de jugadores reales
        if (waitingController != null) {
            waitingController.onServerUpdateReceived(totalPlayers);
        }
        
        // TODO: En el futuro, cuando estemos jugando (Vista 3), pasaremos este JSON 
        // a la pantalla del juego para actualizar las posiciones de los tanques visuals.
    }

    @OnClose
    public void onClose() {
        System.out.println("[Red] Conexión cerrada con el servidor.");
        this.session = null;
    }

    @OnError
    public void onError(Throwable throwable) {
        System.err.println("[Red] Error en el canal de comunicación: " + throwable.getMessage());
    }

    /**
     * Envía comandos al servidor (ej: MOVE_FORWARD, FIRE, STOP_MOVE)
     */
    public void sendMessage(String jsonMessage) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(jsonMessage);
        }
    }

    // Método auxiliar rápido para contar jugadores sin añadir librerías JSON externas
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(substring, idx)) != -1) {
            count++;
            idx += substring.length();
        }
        return count;
    }
}
