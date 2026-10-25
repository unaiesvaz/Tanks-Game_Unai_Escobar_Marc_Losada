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

    // setMainApp: Vincula las referencias de la aplicación y la ventana con esta vista, inicializa los textos informativos en pantalla y registra este controlador en el cliente WebSocket para recibir las notificaciones de red.
    public void setMainApp(Main mainApp, Stage stage) {
        this.mainApp = mainApp;
        this.stage = stage;
        
        statusLabel.setText("Buscando contrincante en el servidor...");
        countdownLabel.setText("");

        // Vinculamos esta pantalla con el cliente de red para enterarnos de cuándo entra el rival
        WebsocketClient.getInstance().setWaitingController(this);
    }

    // onServerUpdateReceived: Recibe el recuento actual de jugadores desde el servidor y, en caso de detectar que ya se han conectado dos o más participantes, activa de forma única e hilada con la interfaz gráfica el inicio de la cuenta atrás.
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

    // startCountdown: Configura y arranca un temporizador con intervalos de un segundo que actualiza un marcador numérico regresivo y, tras mostrar un mensaje de inicio, introduce un breve retardo antes de cargar el mapa de juego.
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
