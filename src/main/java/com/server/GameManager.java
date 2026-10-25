package com.server;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {
    private final ClientRegistry clientRegistry;
    private final ScheduledExecutorService gameLoopExecutor;
    
    // Almacena el estado matemático de todos los tanques activos en la partida
    private final Map<String, TankState> tanks = new ConcurrentHashMap<>();

    private final List<BulletState> bullets = new CopyOnWriteArrayList<>();

    // GameManager: Constructor que inicializa el gestor de partidas vinculando el registro de clientes y preparando el planificador de hilos independiente para el bucle de juego.
    public GameManager(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
        // Creamos un hilo independiente encargado exclusivamente de hacer correr el tiempo del juego
        this.gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    // startGameLoop: Inicia la ejecución periódica del bucle de simulación a una tasa fija de 30 fotogramas por segundo (cada 33 milisegundos).
    public void startGameLoop() {
        gameLoopExecutor.scheduleAtFixedRate(this::updateGame, 0, 33, TimeUnit.MILLISECONDS);
        System.out.println("[GameManager] ¡Bucle de juego en red iniciado a 30 FPS!");
    }

    // addTank: Crea e incorpora un nuevo tanque con su identificador de jugador y coordenadas de inicio en el mapa de tanques activos.
    public void addTank(String playerId, double startX, double startY) {
        tanks.put(playerId, new TankState(playerId, startX, startY));
    }

    // removeTank: Elimina el tanque de un jugador del mapa de juego activo utilizando su identificador único.
    public void removeTank(String playerId) {
        tanks.remove(playerId);
    }

    // getTank: Recupera la instancia del tanque asociado a un identificador de jugador específico para consultar o modificar sus propiedades.
    public TankState getTank(String playerId) {
        return tanks.get(playerId);
    }

    // updateGame: Bucle principal que actualiza el movimiento de los tanques, genera y transmite el estado del juego por red, y procesa la física, rebotes e impactos de todas las balas activas.
    private void updateGame() {
        // 1. Actualizar la posición matemática de todos los tanques que se estén moviendo
        for (TankState tank : tanks.values()) {
            tank.update();
        }

        // 2. Generar el JSON con la estructura exacta que acordamos (STATE)
        String gameStateJson = generateStateJson();
        
        // 3. Enviar este estado actualizado a absolutamente todos los jugadores
        clientRegistry.broadcast(gameStateJson);

        // Actualizar las balas y comprobar los impactos.
        for (BulletState bullet : bullets) {

            if (!bullet.update()) {
                bullets.remove(bullet);
                continue;
            }

            for (TankState target : tanks.values()) {

                // Una bala no puede destruir el tanque que la ha disparado.
                if (!target.isAlive()
                        || target.getId().equals(bullet.getOwnerId())) {
                    continue;
                }

                if (bullet.hits(target)) {
                    target.destroy();
                    bullets.remove(bullet);
                    break;
                }
            }
        }
    }

    // generateStateJson: Construye manualmente una cadena con formato JSON que compila las coordenadas, ángulos y estados actuales de todos los tanques y balas de la partida.
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
            json.append("\"turretAngle\":").append(t.getTurretAngle()).append(",");
            json.append("\"alive\":").append(t.isAlive());
            json.append("}");
            if (i < tanks.size() - 1) {
                json.append(",");
            }
            i++;
        }
        
        json.append("],");

        // Enviar las posiciones actuales de todas las balas.
        List<BulletState> currentBullets = new ArrayList<>(bullets);

        json.append("\"bullets\":[");

        for (i = 0; i < currentBullets.size(); i++) {
            BulletState bullet = currentBullets.get(i);

            json.append("{");
            json.append("\"x\":").append(bullet.getX()).append(",");
            json.append("\"y\":").append(bullet.getY());
            json.append("}");

            if (i < currentBullets.size() - 1) {
                json.append(",");
            }
        }

        json.append("],");
        json.append("\"players\":[]");
        json.append("}");
        
        return json.toString();
    }

    // fire: Gestiona el intento de disparo de un jugador verificando si su tanque está vivo y fuera de tiempo de recarga para instanciar y añadir una nueva bala al juego.
    public void fire(String playerId) {
        TankState tank = tanks.get(playerId);

        if (tank == null || !tank.isAlive()) {
            return;
        }

        if (!tank.tryShoot()) {
            System.out.println(
                "[Disparo] Jugador " + playerId
                + " todavía está recargando."
            );
            return;
        }

        bullets.add(new BulletState(tank));

        System.out.println(
            "[Disparo] Bala creada por " + playerId
        );
    }
    // stopGameLoop: Detiene de forma ordenada el planificador de hilos finalizando el procesamiento continuo del bucle de juego.
    public void stopGameLoop() {
        gameLoopExecutor.shutdown();
        System.out.println("[GameManager] Bucle de juego detenido.");
    }
}
