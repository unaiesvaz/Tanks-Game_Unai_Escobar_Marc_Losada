package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FirstViewController {

    // Estas variables se vinculan automáticamente con los fx:id de Scene Builder
    @FXML
    private TextField nameField;

    @FXML
    private TextField serverField;

    @FXML
    private Label statusLabel;

    private Main mainApp;
    private Stage stage;

    /**
     * Método para conectar la lógica de la clase Main con esta pantalla.
     * Define también los valores que aparecerán escritos por defecto.
     */
    public void setMainApp(Main mainApp, Stage stage) {
        this.mainApp = mainApp;
        this.stage = stage;
        
        if (nameField != null) nameField.setText("Jugador_1");
        if (serverField != null) serverField.setText("ws://localhost:3000/tanks");
    }

    /**
     * Este método se ejecuta automáticamente cuando el usuario hace clic 
     * en el botón verde, gracias al "On Action" que configuramos.
     */
    @FXML
    private void onConnectButtonClick() {
        String name = nameField.getText().trim();
        String url = serverField.getText().trim();

        // Validaciones en español
        if (name.isEmpty() || url.isEmpty()) {
            statusLabel.setText("Error: Los campos no pueden estar vacíos.");
            statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            return;
        }

        if (!url.startsWith("ws://") && !url.startsWith("wss://")) {
            statusLabel.setText("Error: La URL debe empezar por ws:// o wss://");
            statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            return;
        }

        statusLabel.setText("Conectando...");
        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // 1. Iniciamos la conexión WebSocket real con la URL escrita en la interfaz
        WebsocketClient.getInstance().connect(url);

        // 2. Saltamos de forma automática a la pantalla de espera
        mainApp.loadWaitingRoom(stage);
    }

}
