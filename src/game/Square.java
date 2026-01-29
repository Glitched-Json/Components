package game;

import engine.Entity;

@SuppressWarnings("unused")
public class Square extends Entity {
    private boolean flip = false;
    public Square(Number x, Number y, Number z) {
        super("quad", "texture_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }

    @Override
    public void staticUpdate(double dt) {
        rotation.y += 90 * (float) dt;
        if (rotation.y >= 270) {
            rotation.y -= 180;
            //noinspection AssignmentUsedAsCondition
            if (flip = !flip) setShader("shader"); else setShader("texture_shader");
        }
    }
}
