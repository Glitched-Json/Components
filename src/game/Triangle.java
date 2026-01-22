package game;

import engine.Entity;

public class Triangle extends Entity {
    private boolean flip = false;
    public Triangle(Number x, Number y, Number z) {
        super("triangle", "texture_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
        rotation.y = 180;
    }

    @Override
    public void update(double dt) {
        rotation.y += 90 * (float) dt;
        if (rotation.y >= 270) {
            rotation.y -= 180;
            //noinspection AssignmentUsedAsCondition
            if (flip = !flip) setShader("shader"); else setShader("texture_shader");
        }
    }
}
