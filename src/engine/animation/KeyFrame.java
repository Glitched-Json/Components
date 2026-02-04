package engine.animation;

import engine.utils.Entity;
import engine.utils.Vector;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class KeyFrame {
    private Map<String, Vector> fields = new HashMap<>();

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

    public KeyFrame mix(KeyFrame keyFrameA, KeyFrame keyFrameB, float t) {
        set(keyFrameA);
        for (Map.Entry<String, Vector> entry: fields.entrySet())
            if (keyFrameB.fields.containsKey(entry.getKey())) fields.get(entry.getKey()).mul(1-t);

        Map<String, Vector> temp = copyFields(keyFrameB.fields);
        temp.values().forEach(v -> v.mul(t));

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
