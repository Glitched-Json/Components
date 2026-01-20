package game;

import engine.Entity;

public class Square extends Entity {
    public Square() {
        super("quad", "shader");
        position.z = -3;
    }

    @Override
    public void update(double dt) {
        rotation.y += 90 * (float) dt;
    }
}
