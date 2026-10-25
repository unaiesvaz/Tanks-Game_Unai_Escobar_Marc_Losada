package com.project;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

@ClientEndpoint
public class WebsocketClient {

    private static WebsocketClient instance;

    private Session session;
    private volatile String myPlayerId;

    private volatile WaitingViewController waitingController;
    private volatile Consumer<GameState> gameStateListener;

    private WebsocketClient() {
    }
    
    // getInstance: Implementa el patrón Singleton devolviendo la instancia única global del cliente de red, creándola en caso de que no exista previamente.
    public static WebsocketClient getInstance() {
        if (instance == null) {
            instance = new WebsocketClient();
        }
        return instance;
    }

    // setWaitingController: Asigna el controlador de la sala de espera para coordinar las notificaciones de red sobre el número de jugadores conectados.
    public void setWaitingController(
            WaitingViewController controller) {
        this.waitingController = controller;
    }

    // setGameStateListener: Vincula un callback encargado de procesar los cambios de estado de la partida cada vez que llega información del servidor.
    public void setGameStateListener(
            Consumer<GameState> listener) {
        this.gameStateListener = listener;
    }

    // getMyPlayerId: Devuelve el identificador único que el servidor asignó a este cliente durante la negociación de la conexión.
    public String getMyPlayerId() {
        return myPlayerId;
    }

    // connect: Inicia el contenedor WebSocket e intenta establecer una conexión activa y sincronizada con el servidor utilizando la dirección URL proporcionada.
    public synchronized boolean connect(String serverUrl) {
        if (session != null && session.isOpen()) {
            return true;
        }

        myPlayerId = null;

        try {
            WebSocketContainer container =
                    ContainerProvider.getWebSocketContainer();

            System.out.println("[Red] Conectando a: " + serverUrl);

            container.connectToServer(this, URI.create(serverUrl));

            return session != null && session.isOpen();

        } catch (Exception e) {
            System.err.println(
                "[Red] Error al conectar: " + e.getMessage()
            );
            e.printStackTrace();
            return false;
        }
    }

    // disconnect: Realiza el cierre controlado de la sesión de red actual, limpiando la referencia del socket y restableciendo el identificador del jugador.
    public synchronized void disconnect() {
        Session currentSession = session;

        session = null;
        myPlayerId = null;

        if (currentSession != null && currentSession.isOpen()) {
            try {
                currentSession.close();
            } catch (Exception e) {
                System.err.println(
                    "[Red] Error al cerrar la conexión: "
                    + e.getMessage()
                );
            }
        }
    }

    // onOpen: Callback que se ejecuta de forma automática al completarse el apretón de manos inicial del WebSocket, almacenando la sesión de red activa.
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println(
                "[Red] Conexión WebSocket establecida.");
    }

    // onMessage: Captura las transmisiones de texto del servidor, parsea el JSON para extraer los estados de tanques y proyectiles remotos, y notifica tanto a la sala de espera como al bucle visual del juego.
    @OnMessage
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            // El servidor nos comunica nuestro ID real.
            if ("WELCOME".equals(type)) {
                myPlayerId = json.getString("id");

                System.out.println(
                        "[Red] Mi ID de jugador: " + myPlayerId);
                return;
            }

            // Ignorar los mensajes que no sean estados de partida.
            if (!"STATE".equals(type)) {
                return;
            }

            JSONArray tanksJson = json.getJSONArray("tanks");
            List<TankState> tanks = new ArrayList<>();

            for (int i = 0; i < tanksJson.length(); i++) {
                JSONObject tankJson =
                        tanksJson.getJSONObject(i);

                String id = tankJson.getString("id");
                double x = tankJson.getDouble("x");
                double y = tankJson.getDouble("y");
                double angle = tankJson.getDouble("angle");

                // El servidor todavía puede no enviar este campo.
                double turretAngle =
                        tankJson.optDouble(
                                "turretAngle", Double.NaN);

                boolean alive = tankJson.getBoolean("alive");

                tanks.add(new TankState(id, x, y, angle, turretAngle, alive));
            }

            // Leer las balas que envía el servidor
            JSONArray bulletsJson = json.optJSONArray("bullets");
            List<RemoteBulletState> bullets = new ArrayList<>();

            if (bulletsJson != null) {
                for (int i = 0; i < bulletsJson.length(); i++) {
                    JSONObject bulletJson = bulletsJson.getJSONObject(i);

                    double x = bulletJson.getDouble("x");
                    double y = bulletJson.getDouble("y");

                    bullets.add(new RemoteBulletState(x, y));
                }
            }

            // Actualizar la sala de espera con el número de tanques.
            WaitingViewController waiting = waitingController;
            if (waiting != null) {
                waiting.onServerUpdateReceived(tanks.size());
            }

            // Entregar el estado al controlador del juego.
            Consumer<GameState> listener = gameStateListener;
            if (listener != null) {
                listener.accept(new GameState("STATE", tanks, bullets));
            }

        } catch (org.json.JSONException e) {
            System.err.println(
                    "[Red] Error interpretando JSON: "
                            + e.getMessage());
        }
    }

    // onClose: Gestiona el evento de desconexión por parte del servidor o de la red, invalidando las referencias locales de sesión e identificador de jugador.
    @OnClose
    public void onClose(
            Session closedSession,
            jakarta.websocket.CloseReason reason) {

        if (this.session == closedSession) {
            this.session = null;
            this.myPlayerId = null;
        }

        System.out.println(
            "[Red] Conexión cerrada: " + reason
        );
    }

    // onError: Captura y envía a la consola de errores técnicos cualquier fallo o excepción ocurrida en el canal de comunicación del WebSocket.
    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println(
                "[Red] Error WebSocket: " + error.getMessage());
    }

    // sendAction: Envía de forma asíncrona un comando simple empaquetado en JSON (como órdenes de movimiento o disparos) hacia el servidor.
    public void sendAction(String action) {
        JSONObject json = new JSONObject();
        json.put("type", action);
        sendJson(json);
    }

    // sendAim: Envía al servidor un paquete JSON estructurado con las coordenadas físicas del puntero del ratón para actualizar el ángulo de la torreta.
    public void sendAim(double x, double y) {
        JSONObject json = new JSONObject();
        json.put("type", "AIM");
        json.put("x", x);
        json.put("y", y);
        sendJson(json);
    }

    // sendJson: Método privado utilitario que transmite de manera asíncrona cualquier objeto JSON a través de la sesión de texto del WebSocket si la conexión sigue abierta.
    private void sendJson(JSONObject json) {
        Session currentSession = session;

        if (currentSession != null && currentSession.isOpen()) {
            currentSession.getAsyncRemote().sendText(
                    json.toString());
        } else {
            System.out.println(
                    "[Red] No hay conexión con el servidor.");
        }
    }
}