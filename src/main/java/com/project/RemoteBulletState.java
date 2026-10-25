package com.project;

public class RemoteBulletState { //Clase de el estado de la bala (posicion)
    private final double x;
    private final double y;

    public RemoteBulletState(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}