package com.server;

import org.json.JSONObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/tanks")
public class TankEndpoint {

    // Instanciamos el registro y el administrador de forma estática 
    // para que todas las conexiones de red compartan la misma partida.
    private static final ClientRegistry registry = new ClientRegistry();
    private static final GameManager gameManager = new GameManager(registry);

    // static: Bloque estático que inicia el bucle y reloj principal del juego en cuanto el servidor carga la clase del endpoint.
    static {
        gameManager.startGameLoop();
    }

    // onOpen: Gestiona la apertura de una conexión WebSocket registrando al cliente de red, asignándole un tanque en una posición matemática inicial según el orden de entrada y enviándole un mensaje de bienvenida con su identificador único.
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

    // onMessage: Procesa los mensajes JSON recibidos desde el cliente para actualizar el apuntado de la torreta, ejecutar acciones de disparo o modificar los estados de dirección del movimiento del tanque si este sigue con vida.
    @OnMessage
    public void onMessage(String message, Session session) {
        String playerId = session.getId();
        TankState tank = gameManager.getTank(playerId);

        if (tank == null) {
            return;
        }

        try {
            JSONObject json = new JSONObject(message);
            String action = json.optString("type");

            // El apuntado se recibe como coordenadas del ratón.
            if ("AIM".equals(action)) {
                if (tank.isAlive()) {
                    tank.aimAt(
                        json.getDouble("x"),
                        json.getDouble("y")
                    );
                }
                return;
            }

            // Procesar el disparo antes de las acciones de movimiento.
            if ("FIRE".equals(action)) {
                if (tank.isAlive()) {
                    gameManager.fire(playerId);
                }
                return;
            }

            // No permitir movimiento si el tanque ha sido destruido.
            if (!tank.isAlive()) {
                return;
            }

            if ("MOVE_UP".equals(action)
                    || "MOVE_FORWARD".equals(action)) {
                tank.setMovingUp(true);

            } else if ("MOVE_DOWN".equals(action)
                    || "MOVE_BACKWARD".equals(action)) {
                tank.setMovingDown(true);

            } else if ("MOVE_LEFT".equals(action)
                    || "TURN_LEFT".equals(action)) {
                tank.setMovingLeft(true);

            } else if ("MOVE_RIGHT".equals(action)
                    || "TURN_RIGHT".equals(action)) {
                tank.setMovingRight(true);

            } else if ("STOP_UP".equals(action)) {
                tank.setMovingUp(false);

            } else if ("STOP_DOWN".equals(action)) {
                tank.setMovingDown(false);

            } else if ("STOP_LEFT".equals(action)) {
                tank.setMovingLeft(false);

            } else if ("STOP_RIGHT".equals(action)) {
                tank.setMovingRight(false);

            } else if ("STOP_MOVE".equals(action)) {
                tank.stopMoving();
            }

        } catch (Exception e) {
            System.err.println(
                "[Servidor] Error procesando mensaje de "
                + playerId + ": " + e.getMessage()
            );
        }
    }

    // onClose: Realiza la limpieza del jugador cuando este se desconecta de la partida, eliminando tanto su tanque del gestor de juego como su sesión del registro de red de clientes.
    @OnClose
    public void onClose(Session session) {
        String playerId = session.getId();
        // Limpiamos al jugador del juego y de la red al irse
        gameManager.removeTank(playerId);
        registry.unregisterClient(playerId);
    }

    // onError: Captura y registra por la consola de errores cualquier fallo técnico o excepción ocurrida durante la comunicación en la conexión de un jugador específico.
    @OnError
    public void onError(Session session, Throwable throwable) {
        String playerId = session.getId();
        System.err.println("Error en la conexión del jugador " + playerId + ": " + throwable.getMessage());
    }
}
