package com.project;

public class TankState { //Para dar el estado del tanque en determinados puntos de la partida
    //Posicion, a donde apunta, si sigue vivo
    private String id;
    private double x;
    private double y;
    private double angle;
    private double turretAngle;
    private boolean alive;

    public TankState(String id, double x, double y, double angle, double turretAngle, boolean alive) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.turretAngle = turretAngle;
        this.alive = alive;

        
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }

    public double getTurretAngle() {
        return turretAngle;
    }

    public boolean isAlive() {
        return alive;
    }
}
