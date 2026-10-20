package com.project;

import java.util.List;

public class GameState { //Esta clase representara el estado del juego que llega desde el servidor
    private String type;
    private List<TankState> tanks;
    
    public GameState(String type, List<TankState> tanks) {
        this.type = type;
        this.tanks = tanks;
    }

    public String getType() {
        return type;
    }

    public List<TankState> getTanks() {
        return tanks;
    }
}
