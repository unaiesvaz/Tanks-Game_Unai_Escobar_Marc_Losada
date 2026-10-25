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

    // setMainApp: Vincula las referencias de la aplicación principal y de la ventana gráfica a este controlador, estableciendo además los textos por defecto del jugador y del servidor en el formulario.
    public void setMainApp(Main mainApp, Stage stage) {
        this.mainApp = mainApp;
        this.stage = stage;
        
        if (nameField != null) nameField.setText("Jugador_1");
        if (serverField != null) {
            serverField.setText("wss://uescobarvazquez.ieti.site/tanks");
        }
    }

    // onConnectButtonClick: Valida que los campos de texto no estén vacíos y que la dirección del servidor tenga un protocolo válido (ws:// o wss://) para iniciar la conexión por WebSocket y redirigir al usuario a la sala de espera.
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

        mainApp.setConnectionDetails(name, url);

        statusLabel.setText("Conectando...");
        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // 1. Iniciamos la conexión WebSocket real con la URL escrita en la interfaz
        WebsocketClient.getInstance().connect(url);

        // 2. Saltamos de forma automática a la pantalla de espera
        mainApp.loadWaitingRoom(stage);
    }

}
