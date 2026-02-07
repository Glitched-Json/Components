package game;

import engine.utils.Sphere;

@SuppressWarnings("unused")
public class Circle extends Sphere {

    public Circle(Number x, Number y, Number z) { this(x, y, z, 0, 0, 0); }
    public Circle(Number x, Number y, Number z, Number xVelocity, Number yVelocity, Number zVelocity) {
        super(x, y, z, xVelocity, yVelocity, zVelocity);
        diameter = 0.1f;
        speedConservation = 0.6f;
    }

    @Override
    public void update(double dt) {
        if (isClicked()) c.flip(1, 0, 1);
    }
}
