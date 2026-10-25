package com.server;

import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    
    // Guarda las conexiones activas asociando un ID único de jugador con su sesión WebSocket.
    // Se usa ConcurrentHashMap para evitar problemas si varios hilos acceden a la vez.
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    // registerClient: Registra a un nuevo jugador asociando su ID único con su sesión WebSocket activa en el mapa de conexiones.
    public void registerClient(String playerId, Session session) {
        activeSessions.put(playerId, session);
        System.out.println("[Registro] Jugador conectado con ID: " + playerId);
    }

    // unregisterClient: Elimina la sesión de un jugador del mapa de conexiones activas utilizando su ID único.
    public void unregisterClient(String playerId) {
        activeSessions.remove(playerId);
        System.out.println("[Registro] Jugador desconectado con ID: " + playerId);
    }

    // getActiveSessions: Devuelve el mapa que contiene todas las sesiones de los jugadores conectados actualmente.
    public Map<String, Session> getActiveSessions() {
        return activeSessions;
    }

   // broadcast: Envía de forma asíncrona un mensaje de texto a todos los clientes que tienen una sesión WebSocket abierta en ese momento.
    public void broadcast(String message) {
        for (Session session : activeSessions.values()) {
            if (session.isOpen()) {
                // sendText envía el mensaje sin bloquear el hilo principal del juego
                session.getAsyncRemote().sendText(message);
            }
        }
    }
}
