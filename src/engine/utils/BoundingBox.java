package engine.utils;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Matrix3f;
import org.joml.Vector3f;

@Accessors(chain = true)
@NoArgsConstructor
@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class BoundingBox {
    private final Vector3f
        min = new Vector3f(),
        max = new Vector3f(),
        rotation = new Vector3f();
    @Setter private Entity link = null;

    public BoundingBox(Entity link) { setLink(link); }
    public BoundingBox(Number min, Number max) { setBounds(min, max); }
    public BoundingBox(Number min, Number max, Entity link) {
        this.min.set(min.floatValue());
        this.max.set(max.floatValue());
        this.link = link;
    }

    public BoundingBox setBounds(BoundingBox boundingBox) { return setBounds(boundingBox.getMin(), boundingBox.getMax()); }
    public BoundingBox setBounds(Vector3f min, Vector3f max) { this.min.set(min); this.max.set(max); return this; }
    public BoundingBox setBounds(Number min, Number max) { return setMin(min).setMax(max); }

    public Vector3f getMin() { return link == null ? min : link.getPosition().add(min); }
    public BoundingBox setMin(Number value) { return setMin(value, value, value); }
    public BoundingBox setMin(Number x, Number y, Number z) { min.set(x.floatValue(), y.floatValue(), z.floatValue()); return this; }

    public Vector3f getMax() { return link == null ? max : link.getPosition().add(max); }
    public BoundingBox setMax(Number value) { return setMax(value, value, value); }
    public BoundingBox setMax(Number x, Number y, Number z) { max.set(x.floatValue(), y.floatValue(), z.floatValue()); return this; }

    public BoundingBox setRotation(Number degrees) { return setRotation(degrees, degrees, degrees); }
    public BoundingBox setRotation(Number degreeX, Number degreeY, Number degreeZ) { rotation.set(degreeX.floatValue(), degreeY.floatValue(), degreeZ.floatValue()); return this; }

    public Vector3f getSize() { return new Vector3f(max).sub(min); }
    public Vector3f getHalfSize() { return getSize().mul(0.5f); }
    public Vector3f getOrigin() { return new Vector3f(max).add(min).mul(0.5f); }
    public Matrix3f getRotationMatrix() { return new Matrix3f()
            .identity()
            .rotateX((float) Math.toRadians(rotation.x))
            .rotateY((float) Math.toRadians(rotation.y))
            .rotateZ((float) Math.toRadians(rotation.z));
    }
}
