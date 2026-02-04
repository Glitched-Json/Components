package game;

import engine.animation.KeyFrame;
import engine.managers.InputManager;
import engine.utils.Entity;
import engine.utils.Vector;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

@SuppressWarnings("unused")
public class Sprite extends Entity {
    private boolean flip = false, prevFlip = false;
    public Sprite(Number x, Number y, Number z) {
        super("quad", "texture_shader", "card_fool");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
        scale.set(0.28947368421052631578947368421053, 0.5, 0.5);

        animation.addMapping("s", "setState");
        animation.addKeyframe(new KeyFrame());
        animation.addKeyframe(new KeyFrame().setRotation(0, 180, 0).set("s", 1));
    }

    @Override
    public void update(double dt) {
        if (InputManager.isPressed(GLFW_KEY_P))
            if (animation.start()) prevFlip = flip;
    }

    private void setState(Vector vector) {
        if (prevFlip == flip && vector.getFirstFloat() > 0.5) //noinspection AssignmentUsedAsCondition
            texture = (flip = !flip) ? "card_back" : "card_fool";
    }
}
