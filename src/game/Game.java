package game;

import engine.Scene;

public class Game {
    public static void initialize() {
        Scene.get().create(new   Square( 1, 0, -3));
        Scene.get().create(new Triangle(-1, 0, -3));
    }
}
