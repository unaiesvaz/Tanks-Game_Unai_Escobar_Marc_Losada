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

    // BulletState: Constructor que inicializa la posición, dirección y velocidad de la bala a partir del ángulo de la torreta y la ubicación del tanque que la dispara.
    public BulletState(TankState tank) {
        ownerId = tank.getId();

        double radians = Math.toRadians(tank.getTurretAngle());

        dx = Math.cos(radians) * SPEED;
        dy = Math.sin(radians) * SPEED;

        // Crear la bala delante del cañón.
        x = tank.getX() + Math.cos(radians) * 30;
        y = tank.getY() + Math.sin(radians) * 30;
    }

    // update: Actualiza la posición de la bala reduciendo su tiempo de vida, manejando su movimiento en línea recta y calculando rebotes únicos contra los obstáculos antes de decidir si debe destruirse.
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

    // collides: Evalúa si la bala ha colisionado con los límites exteriores del tablero de juego o con alguno de los obstáculos rectangulares definidos en el mapa.
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

    // touchesRectangle: Determina mediante cálculos de distancia si la posición actual de la bala está intersectando el área de un obstáculo rectangular específico.
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

    // hits: Comprueba si la bala se encuentra lo suficientemente cerca de un tanque determinado como para registrar un impacto exitoso.
    public boolean hits(TankState tank) {
        return Math.hypot(
            x - tank.getX(),
            y - tank.getY()
        ) < 22;
    }

    // getOwnerId: Devuelve el identificador único del tanque que disparó la bala.
    public String getOwnerId() {
        return ownerId;
    }

    // getX: Devuelve la coordenada de posición actual en el eje X de la bala.
    public double getX() {
        return x;
    }

    // getY: Devuelve la coordenada de posición actual en el eje Y de la bala.
    public double getY() {
        return y;
    }
}