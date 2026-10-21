package com.server;

public class BulletState {
    private final String ownerId;
    private double x;
    private double y;
    private double dx;
    private double dy;

    private static final double SPEED = 8.0;
    private static final int MAX_TICKS = 100;
    private static final double RADIUS = 4.0;

    private int ticksRemaining = MAX_TICKS;
    private boolean bounced = false;

    public BulletState(TankState tank) {
        ownerId = tank.getId();

        double radians = Math.toRadians(tank.getTurretAngle());

        dx = Math.cos(radians) * SPEED;
        dy = Math.sin(radians) * SPEED;

        // Crear la bala delante del cañón.
        x = tank.getX() + Math.cos(radians) * 30;
        y = tank.getY() + Math.sin(radians) * 30;
    }

    public boolean update() {
        ticksRemaining--;

        if (ticksRemaining <= 0) {
            return false;
        }

        double nextX = x + dx;
        double nextY = y + dy;

        // Detectar colisiones por separado en cada eje.
        boolean hitX = collides(nextX, y);
        boolean hitY = collides(x, nextY);

        // Si solo detectamos una colisión diagonal en la esquina,
        // invertimos ambos sentidos.
        if (!hitX && !hitY && collides(nextX, nextY)) {
            hitX = true;
            hitY = true;
        }

        if (hitX || hitY) {
            // Solo se permite un rebote por bala.
            if (bounced) {
                return false;
            }

            if (hitX) {
                dx = -dx;
            }

            if (hitY) {
                dy = -dy;
            }

            bounced = true;

            // Mantener la bala fuera de la superficie de impacto.
            return true;
        }

        x = nextX;
        y = nextY;

        return true;
    }

    private boolean collides(double testX, double testY) {
        // Paredes exteriores del tablero.
        if (testX - RADIUS < 20
                || testX + RADIUS > 780
                || testY - RADIUS < 20
                || testY + RADIUS > 480) {
            return true;
        }

        // Obstáculos del mapa.
        return touchesRectangle(testX, testY, 260, 300, 60, 100)
                || touchesRectangle(
                    testX, testY, 480, 100, 60, 100);
    }

    private boolean touchesRectangle(
            double px, double py,
            double rectX, double rectY,
            double width, double height) {

        double closestX = Math.max(
            rectX, Math.min(px, rectX + width));

        double closestY = Math.max(
            rectY, Math.min(py, rectY + height));

        return Math.hypot(px - closestX, py - closestY)
                < RADIUS;
    }

    public boolean hits(TankState tank) {
        return Math.hypot(
            x - tank.getX(),
            y - tank.getY()
        ) < 22;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}