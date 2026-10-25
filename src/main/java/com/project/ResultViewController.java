package com.project;

import com.client.UtilsWS;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResultViewController {

    @FXML
    private Label resultLabel;

    @FXML
    private Label winnerDetailLabel; // Se vincula con el nuevo fx:id de Scene Builder

    private Main mainApp;
    private Stage stage;

    // setGameResult: Configura las etiquetas de texto de la pantalla final modificando el título y el mensaje de detalle en español con los nombres de los competidores según si el jugador local ha ganado o perdido.
    public void setGameResult(Main mainApp, Stage stage, boolean isWinner, String myName, String enemyName) {
        this.mainApp = mainApp;
        this.stage = stage;

        // 1. Configurar el título principal para el jugador local
        if (isWinner) {
            resultLabel.setText("¡VICTORIA, " + myName + "!");
            resultLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 36px;");
            
            // Detalle de quién ha ganado en español
            winnerDetailLabel.setText("Has ganado la partida contra " + enemyName);
        } else {
            resultLabel.setText("DERROTA, " + myName);
            resultLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 36px;");
            
            // Detalle de quién ha ganado en español
            winnerDetailLabel.setText("El jugador " + enemyName + " te ha derrotado");
        }
        
        winnerDetailLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");
    }

    // onBackButtonClick: Gestiona la acción de revancha limpiando los oyentes y cerrando la conexión previa para redirigir al usuario a la sala de espera e iniciar una nueva solicitud de conexión por WebSocket al servidor.
    @FXML
    private void onBackButtonClick() {
        System.out.println(
            "[Resultado] Buscando una nueva partida..."
        );

        WebsocketClient client = WebsocketClient.getInstance();

        // Limpiar la conexión anterior por seguridad.
        client.setGameStateListener(null);
        client.setWaitingController(null);
        client.disconnect();

        // Mostrar primero la sala de espera.
        mainApp.loadWaitingRoom(stage);

        // Conectar con la misma dirección configurada al principio.
        boolean connected = client.connect(mainApp.getServerUrl());

        if (!connected) {
            System.err.println(
                "[Resultado] No se ha podido conectar al servidor."
            );
        }
    }
}
