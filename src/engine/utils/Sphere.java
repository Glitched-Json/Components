package engine.utils;

import com.glitched.annotations.Uniform;
import engine.managers.Scene;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Sphere extends Entity {
    public float diameter = 0.5f;
    protected float speedConservation = 1;
    protected final Vector c = new Vector(1, 1, 1);
    protected List<Sphere>
            collisions = new ArrayList<>(),
            prevCollisions = new ArrayList<>();

    public Sphere(Number x, Number y, Number z) { this(x, y, z, 0, 0, 0); }
    public Sphere(Number x, Number y, Number z, Number xVelocity, Number yVelocity, Number zVelocity) {
        super("sphere", "sphere_shader");
        position.set(x.floatValue(), y.floatValue(), z.floatValue());
        speed.set(xVelocity.floatValue(), yVelocity.floatValue(), zVelocity.floatValue());
    }

    @Override public Vector3f getRotation() { return new Vector3f(); }
    @Override public Vector3f getScale() { return new Vector3f(diameter); }

    @Uniform("sphereColor")
    private float[] color() { return c.toFloatArray(); }

    @Override
    public void staticUpdate(double dt) {
        super.staticUpdate(dt);
        updateRigidBodyPhysics();
    }

    protected void updateRigidBodyPhysics() {
        prevCollisions.clear();
        prevCollisions.addAll(collisions);
        if ((collisions = sphereCollisions()).isEmpty()) return;

        Vector3f newSpeed = collisions.stream()
                .filter(s -> !prevCollisions.contains(s))
                .map(s -> getPosition().sub(s.position))
                .reduce(new Vector3f(), Vector3f::add)
                .normalize(speed.length() * speedConservation);
        if (newSpeed.isFinite()) speed.set(newSpeed);
    }

    private List<Sphere> sphereCollisions() {
        return Scene.get().getObjects().stream()
                .filter(e ->
                        Sphere.class.isAssignableFrom(e.getClass())
                        && ((Sphere)e).getDistance(this) < (((Sphere)e).diameter + diameter) / 2
                        && !e.equals(this))
                .map(e -> (Sphere) e)
                .toList();
    }

    private double getDistance(Sphere sphere) {
        Vector3f v = getPosition().sub(sphere.position).absolute();
        return Math.sqrt(v.dot(v));
    }
}
