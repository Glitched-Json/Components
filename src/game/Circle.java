package game;

import engine.managers.InputManager;
import engine.managers.Scene;
import engine.utils.Sphere;
import engine.utils.Vector;

@SuppressWarnings("unused")
public class Circle extends Sphere {
    private final Vector plane;
    public Circle(Number x, Number y, Number z) {
        super(x, y, z);
        plane = new Vector(x, y, z);
        diameter = 0.1f;
    }

    @Override
    public void update(double dt) {
        position.set(InputManager.getIntersectionOrElse(plane, new Vector(Scene.get().getCamera().getFront()), new Vector(0, 0, 0)).toVector3f());
    }

    @Override public void staticUpdate(double dt) {}
}
