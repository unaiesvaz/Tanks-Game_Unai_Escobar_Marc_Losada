package com.server;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/tanks")
public class TankEndpoint {
    
    @OnOpen 
    public void onOpen(Session session) {
        System.out.println("Jugador conectat: " + session.getId());
    }

    @OnMessage 
    public String onMessage(String message, Session session) {
        System.out.println("Missatge rebut del jugador " + session.getId() + ": " + message);
        // Aquí podrías agregar lógica para procesar el mensaje recibido y responder si es necesario
        return "Missatge rebut: " + message;
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Jugador desconnectat: " + session.getId());
    }
}
