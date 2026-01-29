package game;

import engine.Entity;

public class Box extends Entity {
    public Box(Number x, Number y, Number z) {
        super("box", "texture_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }
}
