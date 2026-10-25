package com.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private String playerName = "Jugador_1";
    private String serverUrl = "wss://uescobarvazquez.ieti.site/tanks";

    // setConnectionDetails: Guarda en las variables de la clase las credenciales de conexión introducidas por el usuario (nombre del jugador y dirección URL del servidor).
    public void setConnectionDetails(String playerName, String serverUrl) {
        this.playerName = playerName;
        this.serverUrl = serverUrl;
    }

    // getPlayerName: Devuelve el nombre del jugador configurado actualmente en la aplicación.
    public String getPlayerName() {
        return playerName;
    }

    // getServerUrl: Devuelve la dirección URL del servidor de WebSockets configurada actualmente.
    public String getServerUrl() {
        return serverUrl;
    }

    // start: Punto de entrada de la aplicación JavaFX que define el título, las restricciones de tamaño y el icono de la ventana, cargando la interfaz gráfica inicial del menú de configuración.
    @Override
    public void start(Stage stage) throws Exception {
        // 1. Aplicamos tus configuraciones visuales originales a la ventana
        stage.setTitle("Exemple 0606 - Tanks");
        stage.setResizable(false);

        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));
            stage.getIcons().add(icon);
        }

        // 2. ¡CAMBIO AQUÍ! Cargamos el diseño que has hecho en Scene Builder
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/first_view.fxml"));
        Parent root = loader.load();
        
        // Conectamos el FXML con el controlador de Java
        FirstViewController controller = loader.getController();
        controller.setMainApp(this, stage);

        // Mostramos la escena del menú de configuración inicial
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // loadOriginalGame: Carga el archivo de diseño FXML de la partida, obtiene su controlador, le pasa el contexto del juego y transfiere el enfoque del teclado a la ventana para empezar a jugar.
    public void loadOriginalGame(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/assets/layout.fxml")
            );

            Parent root = loader.load();

            // Obtener el controlador de la partida.
            Controller controller = loader.getController();

            // Pasarle la aplicación, la ventana y el nombre del jugador.
            controller.setGameContext(this, stage, playerName);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            root.requestFocus();

        } catch (Exception e) {
            System.err.println("Error al cargar el juego original: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // loadWaitingRoom: Carga el archivo de diseño FXML de la sala de espera vinculando su respectivo controlador para retener al usuario antes del inicio de la partida.
    public void loadWaitingRoom(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/waiting_view.fxml"));
            Parent root = loader.load();
            
            WaitingViewController controller = loader.getController();
            controller.setMainApp(this, stage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error al cargar la sala de espera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // loadResultView: Carga el archivo de diseño FXML de la pantalla de resultados, transfiriéndole al controlador los datos finales sobre quién venció y los nombres de los competidores.
    public void loadResultView(Stage stage, boolean isWinner, String myName, String enemyName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/result_view.fxml"));
            Parent root = loader.load();
            
            ResultViewController controller = loader.getController();
            // Le pasamos la información detallada de los nombres al controlador
            controller.setGameResult(this, stage, isWinner, myName, enemyName);

            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error al cargar la pantalla de resultados FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // main: Método de arranque principal de la plataforma de Java que invoca la inicialización del ciclo de vida de la aplicación JavaFX.
    public static void main(String[] args) {
        launch(args);
    }
}
