package game;

import com.glitched.annotations.Uniform;
import engine.utils.Entity;
import engine.utils.Vector;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public class Square extends Entity {
    public Square(Number x, Number y, Number z) {
        super("quad", "interact_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }

    @Uniform("Input")
    private float[] input() { return new Vector(new Vector3f(0.3f)).toFloatArray(); }
}
