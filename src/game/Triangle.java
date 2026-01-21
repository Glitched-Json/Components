package game;

import engine.Entity;

public class Triangle extends Entity {
    public Triangle(Number x, Number y, Number z) {
        super("triangle", "inverse_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }

    @Override
    public void update(double dt) {
        rotation.y += 90 * (float) dt;
    }
}
