package com.server;

public class TankState {
    private final String id;
    private double x;
    private double y;
    private double angle;
    private boolean alive;

    // Estados de control que activarán los mensajes JSON del cliente
    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean turningLeft = false;
    private boolean turningRight = false;

    // Velocidades de simulación en el servidor
    private static final double SPEED = 3.0;
    private static final double ROTATION_SPEED = 4.0;

    public TankState(String id, double startX, double startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.angle = 0.0;
        this.alive = true;
    }

    // Este método lo ejecutará el GameManager continuamente en el servidor
    public void update() {
        if (!alive) return;

        // 1. Calcular rotación (Giro)
        if (turningLeft) {
            angle -= ROTATION_SPEED;
        }
        if (turningRight) {
            angle += ROTATION_SPEED;
        }
        angle = (angle % 360 + 360) % 360;

        // 2. Calcular traslación (Avanzar o Retroceder según el ángulo)
        double rad = Math.toRadians(angle);
        
        if (movingForward) {
            x += Math.cos(rad) * SPEED;
            y += Math.sin(rad) * SPEED;
        }
        if (movingBackward) {
            x -= Math.cos(rad) * SPEED;
            y -= Math.sin(rad) * SPEED;
        }
    }

    // Getters para construir el JSON final de respuesta
    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public boolean isAlive() { return alive; }

    // Setters que activaremos cuando el cliente envíe una acción por red
    public void setMovingForward(boolean movingForward) { this.movingForward = movingForward; }
    public void setMovingBackward(boolean movingBackward) { this.movingBackward = movingBackward; }
    public void setTurningLeft(boolean turningLeft) { this.turningLeft = turningLeft; }
    public void setTurningRight(boolean turningRight) { this.turningRight = turningRight; }
}
