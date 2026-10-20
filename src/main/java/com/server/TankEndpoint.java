package com.server;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/tank")
public class TankEndpoint {

    // Instanciamos el registro y el administrador de forma estática 
    // para que todas las conexiones de red compartan la misma partida.
    private static final ClientRegistry registry = new ClientRegistry();
    private static final GameManager gameManager = new GameManager(registry);

    static {
        // En cuanto el servidor arranca esta clase, encendemos el reloj del juego
        gameManager.startGameLoop();
    }

    @OnOpen
    public void onOpen(Session session) {
        // Usamos el ID único que genera el WebSocket para identificar al jugador
        String playerId = session.getId();
        
        // 1. Registramos su conexión de red
        registry.registerClient(playerId, session);

        int numeroJugador = registry.getActiveSessions().size();

        double startX = numeroJugador == 1 ? 100.0 : 700.0;
        double startY = 250.0;

        // 2. Le creamos un tanque matemático en el mapa (ej. posición inicial x: 100, y: 100)
        gameManager.addTank(playerId, startX, startY);

        String welcome = "{\"type\":\"WELCOME\",\"id\":\""+ playerId + "\"}"; //Comunicar al cliente su identificador

        session.getAsyncRemote().sendText(welcome);

        System.out.println(
            "Jugador conectado: " + playerId
            + " | Posición inicial: " + startX + ", " + startY);


        
        
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String playerId = session.getId();
        TankState tank = gameManager.getTank(playerId);

        // Si el jugador no existe o está destruido, ignoramos sus inputs
        if (tank == null || !tank.isAlive()) return;

        // Filtramos el mensaje de texto plano según el protocolo JSON que acordamos.
        // Lo hacemos con contains() para evitar meter librerías de JSON externas.
        if (message.contains("MOVE_FORWARD")) {
            tank.setMovingForward(true);
        } else if (message.contains("MOVE_BACKWARD")) {
            tank.setMovingBackward(true);
        } else if (message.contains("TURN_LEFT")) {
            tank.setTurningLeft(true);
        } else if (message.contains("TURN_RIGHT")) {
            tank.setTurningRight(true);
        } else if (message.contains("STOP_MOVE")) {
            // Apagamos todas las marchas de movimiento cuando el jugador suelta las teclas
            tank.setMovingForward(false);
            tank.setMovingBackward(false);
            tank.setTurningLeft(false);
            tank.setTurningRight(false);
        } else if (message.contains("FIRE")) {
            // Lógica de disparo (se implementará cuando añadamos las balas)
            System.out.println("[Disparo] El jugador " + playerId + " ha disparado.");
        }
    }

    @OnClose
    public void onClose(Session session) {
        String playerId = session.getId();
        // Limpiamos al jugador del juego y de la red al irse
        gameManager.removeTank(playerId);
        registry.unregisterClient(playerId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String playerId = session.getId();
        System.err.println("Error en la conexión del jugador " + playerId + ": " + throwable.getMessage());
    }
}
