package game;

import engine.managers.InputManager;
import engine.utils.Entity;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("unused")
public class Sprite extends Entity {
    public Sprite(Number x, Number y, Number z) {
        super("quad", "texture_shader", "Player");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
        // scale.set(0.28947368421052631578947368421053, 0.5, 0.5);
        scale.set(0.5, 0.5, 0.5);

        spriteAnimation.info(17, 16, 2)
                .addStage( "Idle2",  0,  9, 1   , "Idle2")
                .addStage( "Idle1",  1,  9, 1   , "Idle1")
                .addStage( "Idle0",  2,  9, 1   , "Idle0")
                .addStage( "Dash2",  3,  4, 0.25, "Idle2")
                .addStage( "Dash1",  4,  4, 0.25, "Idle1")
                .addStage( "Dash0",  5,  4, 0.25, "Idle0")
                .addStage("Melee2",  6,  7, 0.5 , "Idle2")
                .addStage("Melee1",  7,  7, 0.5 , "Idle1")
                .addStage("Range2",  8, 11, 1   , "Idle1")
                .addStage("Range1",  9, 12, 1   , "Idle0")
                .addStage( "Hand2", 10,  1, 0.3 , "Idle2")
                .addStage( "Hand1", 11,  1, 0.3 , "Idle1")
                .addStage("Death2", 12, 16, 2                 )
                .addStage("Death1", 13, 16, 2                 )
                .addStage("Death0", 14, 16, 2                 )
                .addStage( "Spawn", 15,  8, 1   , "Idle2")

                .addVariant( "Left", 1)
                .addVariant("Right", 0)

                .addTrigger("Melee2", 3, 0)
                .addTrigger("Melee1", 3, 1)
                .addTrigger( "Dash2", 0, 2)

                .setStage("Spawn");
    }

    @Override
    public void update(double dt) {
        if (InputManager.isPressed(GLFW_KEY_LEFT)) spriteAnimation.setVariant("Left");
        if (InputManager.isPressed(GLFW_KEY_RIGHT)) spriteAnimation.setVariant("Right");

        if (InputManager.isPressed(GLFW_KEY_UP)) spriteAnimation.setStage("Melee2");
        if (InputManager.isPressed(GLFW_KEY_DOWN)) spriteAnimation.setStage("Dash2");

        switch (spriteAnimation.getTrigger()) {
            case 0: System.out.println("Melee hit [2 arms]"); break;
            case 1: System.out.println("Melee hit [1 arm]"); break;
            case 2: System.out.println("Dash"); break;
        }
    }
}
