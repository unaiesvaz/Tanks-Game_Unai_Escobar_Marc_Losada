package com.server;

import java.lang.reflect.Constructor;

public class TankState {
    private final String id;
    private double x;
    private double y;
    private double angle;
    private double turretAngle;
    private boolean alive;

    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;

    private long lastShotTime = 0;

    private static final double SPEED = 3.0;

    // TankState: Constructor que inicializa las propiedades físicas del tanque (ID, coordenadas iniciales, orientación inicial mirando al centro de la pantalla según el bando, ángulos y estado de vida).
    public TankState(String id, double startX, double startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.angle = startX > 400 ? 180 : 0;
        this.turretAngle = this.angle;
        this.alive = true;
    }

    // update: Calcula el movimiento normalizado del tanque en función de las teclas de dirección activas, aplicando detección de colisiones independiente por eje y actualizando el ángulo del chasis.
    public void update() {
        if (!alive) return;

        double dx = 0;
        double dy = 0;

        if (movingLeft) dx--;
        if (movingRight) dx++;
        if (movingUp) dy--;
        if (movingDown) dy++;

        if (dx == 0 && dy == 0) return;

        double length = Math.sqrt(dx * dx + dy * dy);
        dx /= length;
        dy /= length;

        // Intentamos mover el tanque en cada eje por separado.
        // Así puede deslizarse junto a una pared.
        double nextX = x + dx * SPEED;
        double nextY = y + dy * SPEED;

        if (!collides(nextX, y)) {
            x = nextX;
        }

        if (!collides(x, nextY)) {
            y = nextY;
        }

        angle = Math.toDegrees(Math.atan2(dy, dx));
        angle = (angle + 360) % 360;
    }

    // aimAt: Modifica el ángulo de la torreta del tanque calculando la orientación matemática hacia las coordenadas específicas del puntero del ratón enviadas por el jugador.
    public void aimAt(double targetX, double targetY) {
        turretAngle = Math.toDegrees(
            Math.atan2(targetY - y, targetX - x)
        );
        turretAngle = (turretAngle + 360) % 360;
    }

    // getId: Devuelve el identificador único que pertenece al jugador dueño del tanque.
    public String getId() { return id; }
    // getX: Devuelve la posición actual del tanque en el eje X de coordenadas.
    public double getX() { return x; }
    // getY: Devuelve la posición actual del tanque en el eje Y de coordenadas.
    public double getY() { return y; }
    // getAngle: Devuelve la orientación actual en grados que tiene el chasis del tanque.
    public double getAngle() { return angle; }
    // getTurretAngle: Devuelve la orientación actual en grados que tiene la torreta de disparo del tanque.
    public double getTurretAngle() { return turretAngle; }
    // isAlive: Devuelve un valor booleano indicando si el tanque se encuentra activo o destruido en la partida.
    public boolean isAlive() { return alive; }

    // setMovingUp: Modifica el estado de movimiento hacia arriba según la acción de control recibida.
    public void setMovingUp(boolean value) { movingUp = value; }
    // setMovingDown: Modifica el estado de movimiento hacia abajo según la acción de control recibida.
    public void setMovingDown(boolean value) { movingDown = value; }
    // setMovingLeft: Modifica el estado de rotación o desplazamiento hacia la izquierda según la acción de control recibida.
    public void setMovingLeft(boolean value) { movingLeft = value; }
    // setMovingRight: Modifica el estado de rotación o desplazamiento hacia la derecha según la acción de control recibida.
    public void setMovingRight(boolean value) { movingRight = value; }

    // destroy: Cambia el estado vital del tanque marcándolo como muerto para finalizar su simulación física y visual.
    public void destroy() {
        alive = false;
    }

    // stopMoving: Desactiva por completo todas las banderas de dirección deteniendo el desplazamiento del vehículo de inmediato.
    public void stopMoving() {
        movingUp = false;
        movingDown = false;
        movingLeft = false;
        movingRight = false;
    }
    // collides: Evalúa si una posición hipotética del tanque se intercepta con los límites del mapa del juego o con algún obstáculo estático.
    private boolean collides(double testX, double testY) {
        double radius = 18;

        // Paredes exteriores del tablero de 800 × 500.
        if (testX - radius < 20
                || testX + radius > 780
                || testY - radius < 20
                || testY + radius > 480) {
            return true;
        }

        // Obstáculos que aparecen en el mapa del cliente.
        return touchesRectangle(
                    testX, testY, radius,
                    260, 300, 60, 100)
            || touchesRectangle(
                    testX, testY, radius,
                    480, 100, 60, 100);
    }

    // touchesRectangle: Calcula mediante distancias relativas al punto más cercano si el contorno circular del tanque choca contra las dimensiones de una caja u obstáculo rectangular.
    private boolean touchesRectangle(
            double x, double y, double radius,
            double rectX, double rectY,
            double rectWidth, double rectHeight) {

        double closestX = Math.max(
            rectX, Math.min(x, rectX + rectWidth));

        double closestY = Math.max(
            rectY, Math.min(y, rectY + rectHeight));

        double distance = Math.hypot(
            x - closestX, y - closestY);

        return distance < radius;
    }

    // tryShoot: Verifica de forma sincronizada si ha transcurrido el tiempo mínimo de recarga de dos segundos desde el último disparo antes de habilitar el nacimiento de una bala.
    public synchronized boolean tryShoot() {
        long now = System.currentTimeMillis();

        if (now - lastShotTime < 2000) {
            return false;
        }

        lastShotTime = now;
        return true;
    }
}