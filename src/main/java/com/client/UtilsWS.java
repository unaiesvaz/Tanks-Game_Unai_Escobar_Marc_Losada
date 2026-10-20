package com.client;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import org.json.JSONObject;

import org.json.JSONArray;
import com.project.TankState;
import com.project.GameState;
import java.util.ArrayList;
import java.util.List;

public class UtilsWS { //Clase encargada de gestionar la conexion entre cliente - servidor
    private static UtilsWS instance;

    private final HttpClient client; //Iniciar conexion de webSockets
    private WebSocket webSocket; 

    private java.util.function.Consumer<GameState> stateListener; //Permite guardar una funcion que se ejecutara cuando llegue un estado de partida


    private UtilsWS() {
        client = HttpClient.newHttpClient();
    }

    public void connect(String serverUrl) { //Este metodo inicia la conexion con el servidor 
        WebSocket.Listener listener = new WebSocket.Listener() {

        @Override
        public void onOpen(WebSocket webSocket) { //Se ejecuta cuando se establece la conexion 
            UtilsWS.this.webSocket = webSocket;

            System.out.println("Conexión WebSocket establecida.");

            webSocket.request(1);
        }
        
        @Override
        public java.util.concurrent.CompletionStage<?> onText( //Se ejecuta cuando llega un mensaje al servidor 
            WebSocket webSocket, CharSequence data, boolean last) {

            System.out.println("Mensaje recibido: " + data);

            try {
                GameState state = parseGameState(data.toString()); //Para coger el estado del juego

                if (state != null) {

                    if (stateListener != null) {
                        stateListener.accept(state);
                    }
                    System.out.println("Estado de partida recibido.");

                    for (TankState tank : state.getTanks()) { 
                        System.out.println(
                            "Tanque: " + tank.getId()
                            + " | X: " + tank.getX()
                            + " | Y: " + tank.getY()
                            + " | Ángulo: " + tank.getAngle()
                            + " | Vivo: " + tank.isAlive()
                        );
                    }
                }

            } catch (org.json.JSONException e) {
                System.err.println(
                    "Error al interpretar el mensaje JSON: " + e.getMessage()
                );
            }

            webSocket.request(1);

            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }



        @Override
        public java.util.concurrent.CompletionStage<?> onClose( //Al cerrar la conexion se ejecuta 
                WebSocket webSocket, int statusCode, String reason) {

            System.out.println("Conexión cerrada: " + reason);

            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }


        @Override
        public void onError(WebSocket webSocket, Throwable error) { //Si ocurre cualquier error en la comunicacion, se ejecuta esta parte
            System.err.println("Error WebSocket: " + error.getMessage());
        }
    };

    client.newWebSocketBuilder()
            .buildAsync(java.net.URI.create(serverUrl), listener)
            .exceptionally(error -> {
                System.err.println(
                        "No se pudo conectar: " + error.getMessage());
                return null;
            });
    }
    
    public static UtilsWS getInstance() { //Si instance esta vacia, crea el objeto 
        if (instance == null) {
            instance = new UtilsWS();
        }

        return instance;
    }

    public void setStateListener(
        java.util.function.Consumer<GameState> listener) {
        this.stateListener = listener;
    }

    public void sendMessage(String message) { //Recibe el mensaje que queremos enviar al servidor 
        if (webSocket != null) {
            webSocket.sendText(message, true);
        } else {
            System.out.println("No hay conexión con el servidor.");
        }
    }

    public void sendJson(JSONObject message) { //Para convertir el objeto Json en texto
        if (webSocket != null) {
            webSocket.sendText(message.toString(), true);
        } else {
            System.out.println("No hay conexión con el servidor.");
        }
    }

    
    public void sendAim(double x, double y) { //Es para enviar la informacion a la hora de apuntar al servidor 
        JSONObject message = new JSONObject(); //Objeto json vacio

        message.put("type", "AIM");//El tipo de accion es AIM
        message.put("x", x);
        message.put("y", y);//Agrega las coordenadas 

        sendJson(message); // Enviamos el mensaje al servidor 
    }

    /*
    sendAction, recibira como argumentos:
    MOVE_FORWARD
    MOVE_BACKWARD
    TURN_LEFT 
    TURN_RIGHT
    STOP_MOVE
    AIM
    FIRE
    */

    public void sendAction(String action) { //Recibe el nombre de la accion mediante action 
        JSONObject message = new JSONObject(); //Crea json vacio 

        message.put("type", action); // Como propiedad agrega type con el nombre de la accion 

        sendJson(message); //Envia el mensaje 
    }

    private GameState parseGameState(String json) { //Este metodo nos sirve para convertir el mensaje JSON que recibimos del server en un objeto para poder utilizarlo
        JSONObject message = new JSONObject(json);  

        String type = message.getString("type");

        if (!type.equals("STATE")) { // Comprobar que es un mensaje de estado de partida
            return null;
        }

        JSONArray tanksJson = message.getJSONArray("tanks"); // Obtener el array de tanques del JSON
        List<TankState> tanks = new ArrayList<>();

        for (int i = 0; i < tanksJson.length(); i++) { // Recorrer los tanques recibidos
            JSONObject tankJson = tanksJson.getJSONObject(i);

            String id = tankJson.getString("id");
            double x = tankJson.getDouble("x");
            double y = tankJson.getDouble("y");
            double angle = tankJson.getDouble("angle");
            double turretAngle = tankJson.getDouble("turretAngle");
            boolean alive = tankJson.getBoolean("alive");

            TankState tank = new TankState(id, x, y, angle,turretAngle, alive); // Crear el objeto que representa al tanque

            tanks.add(tank);
        }

        return new GameState(type, tanks); // Crear y devolver el estado de la partida
    }


}
