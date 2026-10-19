package com.client;

import java.net.http.HttpClient;
import java.net.http.WebSocket;

public class UtilsWS { //Clase encargada de gestionar la conexion entre cliente - servidor
    private static UtilsWS instance;

    private final HttpClient client; //Iniciar conexion de webSockets
    private WebSocket webSocket; 

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

    public void sendMessage(String message) { //Recibe el mensaje que queremos enviar al servidor 
        if (webSocket != null) {
            webSocket.sendText(message, true);
        } else {
            System.out.println("No hay conexión con el servidor.");
        }
    }

}
