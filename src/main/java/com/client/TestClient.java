
package com.client;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;

@ClientEndpoint
public class TestClient {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Conectado al servidor Tanks.");

        try {
            session.getBasicRemote().sendText("Hola, soy el jugador de prueba");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Respuesta del servidor: " + message);
    }

    public static void main(String[] args) throws Exception {
        WebSocketContainer container =
                ContainerProvider.getWebSocketContainer();

        Session session = container.connectToServer(
                TestClient.class,
                URI.create("ws://localhost:3000/tanks")
        );

        System.out.println("Conexión establecida. Pulsa Enter para salir.");
        System.in.read();

        session.close();
        System.out.println("Cliente desconectado.");
    }
}