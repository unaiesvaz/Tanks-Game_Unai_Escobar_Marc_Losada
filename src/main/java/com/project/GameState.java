package com.project;

import java.util.List;

public class GameState {
    private final String type;
    private final List<TankState> tanks;
    private final List<RemoteBulletState> bullets;
    

    public GameState(String type, List<TankState> tanks) {
        this(type, tanks, List.of());
    }

    public GameState(
            String type,
            List<TankState> tanks,
            List<RemoteBulletState> bullets) {
        this.type = type;
        this.tanks = tanks;
        this.bullets = bullets;
    }

    public String getType() {
        return type;
    }

    public List<TankState> getTanks() {
        return tanks;
    }

    public List<RemoteBulletState> getBullets() {
        return bullets;
    }
}
