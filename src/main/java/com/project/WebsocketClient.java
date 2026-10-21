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

    public static WebsocketClient getInstance() {
        if (instance == null) {
            instance = new WebsocketClient();
        }
        return instance;
    }

    public void setWaitingController(
            WaitingViewController controller) {
        this.waitingController = controller;
    }

    public void setGameStateListener(
            Consumer<GameState> listener) {
        this.gameStateListener = listener;
    }

    public String getMyPlayerId() {
        return myPlayerId;
    }

    public void connect(String serverUrl) {
        try {
            WebSocketContainer container =
                    ContainerProvider.getWebSocketContainer();

            System.out.println(
                    "[Red] Conectando a: " + serverUrl);

            container.connectToServer(
                    this, URI.create(serverUrl));

        } catch (Exception e) {
            System.err.println(
                    "[Red] Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println(
                "[Red] Conexión WebSocket establecida.");
    }

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

    @OnClose
    public void onClose(Session session,jakarta.websocket.CloseReason reason) {
        this.session = null;
        System.out.println(
                "[Red] Conexión cerrada: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println(
                "[Red] Error WebSocket: " + error.getMessage());
    }

    public void sendAction(String action) {
        JSONObject json = new JSONObject();
        json.put("type", action);
        sendJson(json);
    }

    public void sendAim(double x, double y) {
        JSONObject json = new JSONObject();
        json.put("type", "AIM");
        json.put("x", x);
        json.put("y", y);
        sendJson(json);
    }

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