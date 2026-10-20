package com.project;

public class TankState {
    private String id;
    private double x;
    private double y;
    private double angle;
    private boolean alive;

    public TankState(String id, double x, double y, double angle, boolean alive) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.angle = angle;
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

    public boolean isAlive() {
        return alive;
    }
}
