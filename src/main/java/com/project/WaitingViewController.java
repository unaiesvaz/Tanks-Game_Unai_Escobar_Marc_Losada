package com.project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingViewController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label countdownLabel;

    private Main mainApp;
    private Stage stage;
    private int secondsRemaining = 5;
    private boolean countdownStarted = false; // Evita que la cuenta atrás se active más de una vez

    public void setMainApp(Main mainApp, Stage stage) {
        this.mainApp = mainApp;
        this.stage = stage;
        
        // Inicializamos los textos en español limpios
        statusLabel.setText("Buscando contrincante en el servidor...");
        countdownLabel.setText("");
    }

    /**
     * Este método lo llamará tu sistema de red cada vez que llegue un JSON del servidor.
     * @param totalPlayers El número de tanques activos devueltos por el JSON de STATE.
     */
    public void onServerUpdateReceived(int totalPlayers) {
        // Ejecutamos en el hilo de la interfaz de JavaFX
        Platform.runLater(() -> {
            // Si hay 2 jugadores y aún no ha empezado la cuenta atrás, la iniciamos
            if (totalPlayers >= 2 && !countdownStarted) {
                countdownStarted = true;
                statusLabel.setText("¡Contrincante conectado! Iniciando batalla...");
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                startCountdown();
            }
        });
    }

    private void startCountdown() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (secondsRemaining > 0) {
                        countdownLabel.setText(String.valueOf(secondsRemaining));
                        secondsRemaining--;
                    } else {
                        timer.cancel();
                        countdownLabel.setText("¡YA!");
                        
                        // Retardo de medio segundo para que se vea el "¡YA!" antes de abrir el mapa
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> mainApp.loadOriginalGame(stage));
                            }
                        }, 500);
                    }
                });
            }
        }, 0, 1000); // Un tic cada segundo
    }
}
