package com.server;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    
    // Guarda las conexiones activas asociando un ID único de jugador con su sesión WebSocket.
    // Usamos ConcurrentHashMap para evitar problemas si varios hilos acceden a la vez.
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    // Registra a un nuevo jugador cuando abre la conexión
    public void registerClient(String playerId, Session session) {
        activeSessions.put(playerId, session);
        System.out.println("[Registro] Jugador conectado con ID: " + playerId);
    }

    // Elimina al jugador cuando se cierra la conexión o hay un error
    public void unregisterClient(String playerId) {
        activeSessions.remove(playerId);
        System.out.println("[Registro] Jugador desconectado con ID: " + playerId);
    }

    // Devuelve el mapa completo por si necesitamos consultar las conexiones
    public Map<String, Session> getActiveSessions() {
        return activeSessions;
    }

    /**
     * Hace un BROADCAST. Toma cualquier mensaje (como nuestro JSON de STATE)
     * y se lo envía de manera asíncrona a TODOS los clientes conectados.
     */
    public void broadcast(String message) {
        for (Session session : activeSessions.values()) {
            if (session.isOpen()) {
                // sendText envía el mensaje sin bloquear el hilo principal del juego
                session.getAsyncRemote().sendText(message);
            }
        }
    }
}
