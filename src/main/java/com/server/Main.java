package com.server;

// Importa la clase Server de la librería Tyrus para poder crear el servidor WebSocket
import org.glassfish.tyrus.server.Server;

public class Main {
    
    // main: Configura, arranca y mantiene en ejecución un servidor WebSocket en el puerto 3000 bajo la ruta raíz, enlazándolo a la lógica de conexiones del juego y asegurando la liberación del puerto al finalizar.

    public static void main(String[] args) {
        /* 
         * Configura el servidor de WebSockets:
         * - "0.0.0.0": Escucha en todas las interfaces de red (permite conexiones externas y locales).
         * - 3000: Puerto de red asignado para la conexión (ws://localhost:3000/).
         * - "/": Ruta raíz de la conexión.
         * - null: Configuración de propiedades adicionales (no se necesitan en este caso).
         * - TankEndpoint.class: Clase encargada de gestionar los eventos y la lógica del juego de tanques.
         */
        Server server = new Server("0.0.0.0", 3000, "/", null, TankEndpoint.class);
        
        try {
            // Activa y arranca el servidor en el puerto especificado
            server.start();
            
            // Mensajes informativos por consola para confirmar que todo funciona
            System.out.println("Servidor Tanks iniciat en el port 3000.");
            System.out.println("Esperant connexions de jugadors...");

            /* 
             * Pausa y bloquea el hilo principal (main) indefinidamente. 
             * Esto evita que el programa Java termine inmediatamente y mantiene el servidor encendido.
             */
            Thread.currentThread().join();
            
        } catch (Exception e) {
            // Captura y muestra cualquier error que ocurra durante el inicio o ejecución del servidor
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Bloque seguro: garantiza que el servidor se detenga y libere el puerto 3000 al cerrar la aplicación
            server.stop();
        }
    }
    
}