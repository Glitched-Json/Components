package game;

import com.glitched.annotations.Uniform;
import engine.Entity;
import engine.SpatialManager;
import org.joml.Vector3f;

public class Box extends Entity {
    private final Vector3f c = new Vector3f(1, 1, 1);
    public Box(Number x, Number y, Number z) {
        super("box", "interact_shader");
        // super("box", "spatial_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }

    @Override
    public void update(double dt) {
        if (SpatialManager.getId() == getId()) c.set(0, 1, 0); else c.set(1, 0, 0);
    }

    @Uniform("Input")
    @SuppressWarnings("unused")
    private float[] color() {
        return new float[]{c.x, c.y, c.z};
    }
}
