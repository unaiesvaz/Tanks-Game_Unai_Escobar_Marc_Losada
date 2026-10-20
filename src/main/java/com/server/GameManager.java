package com.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {
    private final ClientRegistry clientRegistry;
    private final ScheduledExecutorService gameLoopExecutor;
    
    // Almacena el estado matemático de todos los tanques activos en la partida
    private final Map<String, TankState> tanks = new ConcurrentHashMap<>();

    public GameManager(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
        // Creamos un hilo independiente encargado exclusivamente de hacer correr el tiempo del juego
        this.gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    // Inicia el bucle de simulación a 30 fotogramas por segundo (cada 33 milisegundos)
    public void startGameLoop() {
        gameLoopExecutor.scheduleAtFixedRate(this::updateGame, 0, 33, TimeUnit.MILLISECONDS);
        System.out.println("[GameManager] ¡Bucle de juego en red iniciado a 30 FPS!");
    }

    // Métodos para que el Endpoint añada o quite tanques cuando la gente se conecte/desconecte
    public void addTank(String playerId, double startX, double startY) {
        tanks.put(playerId, new TankState(playerId, startX, startY));
    }

    public void removeTank(String playerId) {
        tanks.remove(playerId);
    }

    // Permite obtener el tanque de un jugador específico para cambiar sus booleanos de dirección
    public TankState getTank(String playerId) {
        return tanks.get(playerId);
    }

    /**
     * El bucle principal. Se ejecuta cada 33ms automáticamente.
     */
    private void updateGame() {
        // 1. Actualizar la posición matemática de todos los tanques que se estén moviendo
        for (TankState tank : tanks.values()) {
            tank.update();
        }

        // 2. Generar el JSON con la estructura exacta que acordamos (STATE)
        String gameStateJson = generateStateJson();
        
        // 3. Enviar este estado actualizado a absolutamente todos los jugadores
        clientRegistry.broadcast(gameStateJson);
    }

    /**
     * Construye manualmente el JSON tipo "STATE" para enviárselo al cliente.
     * Nota: Si usas librerías como Jackson o Gson en tu proyecto, se puede simplificar,
     * pero construyéndolo como String nos aseguramos de que funcione sin importar tus dependencias.
     */
    private String generateStateJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"type\":\"STATE\",");
        json.append("\"tanks\":[");
        
        int i = 0;
        for (TankState t : tanks.values()) {
            json.append("{");
            json.append("\"id\":\"").append(t.getId()).append("\",");
            json.append("\"x\":").append(t.getX()).append(",");
            json.append("\"y\":").append(t.getY()).append(",");
            json.append("\"angle\":").append(t.getAngle()).append(",");
            json.append("\"alive\":").append(t.isAlive());
            json.append("}");
            if (i < tanks.size() - 1) {
                json.append(",");
            }
            i++;
        }
        
        json.append("],");
        json.append("\"bullets\":[],"); // Se gestionarán más adelante
        json.append("\"players\":[]");
        json.append("}");
        
        return json.toString();
    }

    public void stopGameLoop() {
        gameLoopExecutor.shutdown();
        System.out.println("[GameManager] Bucle de juego detenido.");
    }
}
