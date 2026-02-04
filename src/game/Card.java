package game;

import engine.managers.InputManager;
import engine.utils.Entity;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@SuppressWarnings("unused")
public class Card extends Entity {
    boolean flip = false;
    public Card(Number x, Number y, Number z) {
        super("quad", "texture_shader", "card_back");
        scale.set(0.57894736842105263157894736842105, 1, 1);
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }

    @Override
    public void update(double dt) {
        if (isHighlighted() && InputManager.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) //noinspection AssignmentUsedAsCondition
            texture = (flip = !flip) ? "card_fool" : "card_back";
    }
}
