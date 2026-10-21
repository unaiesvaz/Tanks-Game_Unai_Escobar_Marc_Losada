package com.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

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

    /**
     * Este método contiene EXACTAMENTE tu código original.
     * Lo llamaremos desde el FirstViewController cuando el jugador pulse el botón de conectar.
     */
    public void loadOriginalGame(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/assets/layout.fxml"));
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            // Enfoca la ventana para que el teclado funcione al instante en el juego
            root.requestFocus(); 
        } catch (Exception e) {
            System.err.println("Error al cargar el juego original: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    /**
     * Carga la cuarta vista de resultados mostrando quién ha ganado y permitiendo revancha.
     */
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


    public static void main(String[] args) {
        launch(args);
    }
}
