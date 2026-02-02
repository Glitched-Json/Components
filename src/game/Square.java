package game;

import engine.Entity;

@SuppressWarnings("unused")
public class Square extends Entity {
    public Square(Number x, Number y, Number z) {
        super("quad", "texture_shader", "arrow");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
    }
}
