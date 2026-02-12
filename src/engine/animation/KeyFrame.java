package engine.animation;

import engine.utils.Entity;
import engine.utils.Vector;
import lombok.Getter;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class KeyFrame {
    public static final int
            LINEAR = 0,

            EASE_IN = 1,
            EASE_OUT = 2,
            EASE_IN_OUT = 3,

            EASE_IN_BACK = 4,
            EASE_OUT_BACK = 5,
            EASE_IN_OUT_BACK = 6,

            EASE_IN_ELASTIC = 7,
            EASE_OUT_ELASTIC = 8,
            EASE_IN_OUT_ELASTIC = 9;

    private Map<String, Vector> fields = new HashMap<>();
    @Getter private final double time;
    private int mode = LINEAR;
    private double power = 1;

    public KeyFrame() { this(1); }
    public KeyFrame(Number time) { this.time = Math.max(time.doubleValue(), 1e-9); }

    public KeyFrame clear() { fields.clear(); return this; }
    public KeyFrame set(String field, Number... values) {
        if (fields.containsKey(field)) { fields.get(field).setAll(values); }
        else fields.put(field, new Vector(values));
        return this;
    }
    public KeyFrame set(KeyFrame keyFrame) {
        fields = copyFields(keyFrame.fields);
        return this;
    }
    public KeyFrame setPos(Vector pos)       { return setPos(pos.toVector3f()); }
    public KeyFrame setPos(Vector3f pos)     { return set("pos", pos.x, pos.y, pos.z); }
    public KeyFrame setPos(Number... values) { return set("pos", values); }
    public KeyFrame setRotation(Vector rotation)   { return setRotation(rotation.toVector3f()); }
    public KeyFrame setRotation(Vector3f rotation) { return set("rotation", rotation.x, rotation.y, rotation.z); }
    public KeyFrame setRotation(Number... values)  { return set("rotation", values); }
    public KeyFrame setScale(Vector scale)     { return setScale(scale.toVector3f()); }
    public KeyFrame setScale(Vector3f scale)   { return set("size", scale.x, scale.y, scale.z); }
    public KeyFrame setScale(Number... values) { return set("size", values); }
    public KeyFrame setMode(int mode) { return setMode(mode, 1); }
    public KeyFrame setMode(int mode, Number power) { this.mode = mode; this.power = Math.max(power.doubleValue(), 1e-6); return this; }

    public KeyFrame mix(KeyFrame keyFrameA, KeyFrame keyFrameB, double t) {
        t /= keyFrameB.time;
        final double timePower = t * keyFrameB.power;
        final double pow = keyFrameB.power;
        final double safePower =
                (pow >= 0 && pow < 1e-3)
                ? 1e-3 : (pow < 0 && pow >-1e-3)
                ? -1e-3 : pow;
        final double tVar = switch (keyFrameB.mode) {
            case EASE_IN -> timePower / (timePower - t + 1);
            case EASE_OUT -> -t / (timePower - pow - t);
            case EASE_IN_OUT -> t < 0.5
                    ? timePower / (2*(timePower - t + 0.5))
                    : (0.5 - t) / (2*(timePower - pow - t + 0.5)) + 0.5;

            case EASE_IN_BACK -> (pow + 1)*t*t*t - timePower*t;
            case EASE_OUT_BACK -> 1 + (pow + 1) * Math.pow(t-1, 3) + pow * Math.pow(t-1, 2);
            case EASE_IN_OUT_BACK -> t < 0.5
                    ? (2*t)*(2*t) * ((pow + 1) * 2 * t - pow) / 2
                    : ((2*t-2)*(2*t-2) * ((pow + 1) * (t * 2 - 2) + pow) + 2) / 2;

            case EASE_IN_ELASTIC -> -Math.pow(2, 10*t-10) * Math.sin((t*10-10.75) * 2 * Math.PI / safePower);
            case EASE_OUT_ELASTIC -> Math.pow(2, -10*t) * Math.sin((t*10 - 0.75) * 2 * Math.PI / safePower) + 1;
            case EASE_IN_OUT_ELASTIC -> {
                double v = Math.sin((20 * t - 11.125) * 2 * Math.PI / safePower);
                yield t < 0.5
                        ? -(Math.pow(2, 20*t-10) * v) / 2
                        : (Math.pow(2, -20*t+10) * v) / 2 + 1;
            }

            default -> t;
        };

        set(keyFrameA);
        for (Map.Entry<String, Vector> entry: fields.entrySet())
            if (keyFrameB.fields.containsKey(entry.getKey())) fields.get(entry.getKey()).mul(1-tVar);

        Map<String, Vector> temp = copyFields(keyFrameB.fields);
        temp.values().forEach(v -> v.mul(tVar));

        add(temp);

        return this;
    }

    public KeyFrame add(KeyFrame keyFrame) {
        add(keyFrame.fields);
        return this;
    }

    private void add(Map<String, Vector> values) {
        for (Map.Entry<String, Vector> entry: values.entrySet())
            if (fields.containsKey(entry.getKey())) fields.get(entry.getKey()).add(entry.getValue());
            else fields.put(entry.getKey(), entry.getValue());
    }

    private Map<String, Vector> copyFields(Map<String, Vector> fields) {
        Map<String, Vector> map = new HashMap<>();
        for (Map.Entry<String, Vector> entry: fields.entrySet()) map.put(entry.getKey(), new Vector(entry.getValue()));
        return map;
    }

    public Vector get(String key) { if (fields.containsKey(key)) return fields.get(key); return new Vector(0); }

    public void apply(KeyFrame previousFrame, Entity entity, Map<String, AnimationMapping> mappings) {
        for (String field: fields.keySet()) {
            if (mappings.containsKey(field)) try {
                Vector v;
                if (mappings.get(field).relative()) v = new Vector(fields.get(field)).sub(previousFrame.get(field));
                else v = fields.get(field);

                mappings.get(field).method().setAccessible(true);
                mappings.get(field).method().invoke(entity, v);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
    }
}
