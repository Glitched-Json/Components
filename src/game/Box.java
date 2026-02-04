package game;

import com.glitched.annotations.Uniform;
import engine.animation.KeyFrame;
import engine.utils.Entity;
import engine.utils.Vector;

public class Box extends Entity {
    private final Vector c = new Vector(1, 1, 1);
    public Box(Number x, Number y, Number z) {
        super("box", "interact_shader");
        // super("box", "texture_shader", "arrow");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());

        animation.addMapping("c", "setColor");
        animation.addKeyframe(new KeyFrame().setPos(0,  0, 0).setRotation(0, 0,  0).set("c", 1  , 1  , 1));
        animation.addKeyframe(new KeyFrame().setPos(0,  1, 0).setRotation(0, 0, 45).set("c", 1  , 0  , 0));
        animation.addKeyframe(new KeyFrame().setPos(0,  0, 0).setRotation(0, 0, 45).set("c", 0.5, 0.5, 0));
        animation.addKeyframe(new KeyFrame().setPos(0, -1, 0).setRotation(0, 0, 45).set("c", 0  , 1  , 0));
        animation.addKeyframe(new KeyFrame().setPos(0,  0, 0).setRotation(0, 0,  0).set("c", 1  , 1  , 1));
    }

    @Override
    public void update(double dt) {
        if (isClicked()) // c.flip(1, 0, 1);
            animation.start();
    }

    @Uniform("Input")
    @SuppressWarnings("unused")
    private float[] color() {
        return c.toFloatArray();
    }

    @SuppressWarnings("unused")
    private void setColor(Vector v) { c.set(v); }
}
