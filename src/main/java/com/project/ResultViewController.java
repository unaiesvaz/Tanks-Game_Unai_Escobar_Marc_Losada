package com.project;

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

    /**
     * Configura la pantalla final mostrando si has ganado/perdido y el nombre del ganador.
     */
    public void setGameResult(Main mainApp, Stage stage, boolean isWinner, String myName, String enemyName) {
        this.mainApp = mainApp;
        this.stage = stage;

        // 1. Configurar el título principal para el jugador local
        if (isWinner) {
            resultLabel.setText("¡VICTORIA!");
            resultLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 36px;");
            
            // Detalle de quién ha ganado en español
            winnerDetailLabel.setText("Has ganado la partida contra " + enemyName);
        } else {
            resultLabel.setText("DERROTA");
            resultLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 36px;");
            
            // Detalle de quién ha ganado en español
            winnerDetailLabel.setText("El jugador " + enemyName + " te ha derrotado");
        }
        
        winnerDetailLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");
    }

    /**
     * Acción del botón: reconecta de forma automática y te manda a la sala de espera (Vista 2)
     */
    @FXML
    private void onBackButtonClick() {
        System.out.println("[Resultado] Volviendo a buscar partida. Redirigiendo a la Sala de Espera...");
        
        // 1. Volvemos a conectar nuestro cliente WebSocket para buscar un rival (mismo o diferente)
        // Usamos la URL que ya guardamos en los pasos iniciales
        String serverUrl = UtilsWS.getInstance().getServerUrl(); 
        if (serverUrl != null) {
            UtilsWS.getInstance().connect(serverUrl);
        }

        // 2. Saltamos directamente a la Segunda Vista (Sala de Espera)
        mainApp.loadWaitingRoom(stage);
    }
}
