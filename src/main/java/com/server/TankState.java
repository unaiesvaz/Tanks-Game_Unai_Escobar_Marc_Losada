package com.server;

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

    public TankState(String id, double startX, double startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.angle = startX > 400 ? 180 : 0;
        this.turretAngle = this.angle;
        this.alive = true;
    }

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

    // Actualizar la dirección de la torreta según el ratón del jugador.
    public void aimAt(double targetX, double targetY) {
        turretAngle = Math.toDegrees(
            Math.atan2(targetY - y, targetX - x)
        );
        turretAngle = (turretAngle + 360) % 360;
    }

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public double getTurretAngle() { return turretAngle; }
    public boolean isAlive() { return alive; }

    public void setMovingUp(boolean value) { movingUp = value; }
    public void setMovingDown(boolean value) { movingDown = value; }
    public void setMovingLeft(boolean value) { movingLeft = value; }
    public void setMovingRight(boolean value) { movingRight = value; }

    public void destroy() {
        alive = false;
    }

    public void stopMoving() {
        movingUp = false;
        movingDown = false;
        movingLeft = false;
        movingRight = false;
    }

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

    public synchronized boolean tryShoot() {
        long now = System.currentTimeMillis();

        if (now - lastShotTime < 2000) {
            return false;
        }

        lastShotTime = now;
        return true;
    }
}