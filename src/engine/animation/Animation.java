package engine.animation;

import engine.utils.Entity;
import engine.utils.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {
    private final Entity entity;
    private final Map<String, AnimationMapping> mappings = new HashMap<>(){
        private void addMapping(String key, String method) {
            try { put(key, new AnimationMapping(true, Entity.class.getDeclaredMethod(method, Vector.class))); }
            catch (NoSuchMethodException ignored) {}
        }

        {
            addMapping("pos", "move");
            addMapping("rotation", "rotate");
            addMapping("size", "setScale");
        }
    };

    public Animation(Entity entity) {
        this.entity = entity;
    }

    private final KeyFrame v = new KeyFrame(), vPrev = new KeyFrame();
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private double t;
    private boolean running = false;
    public void start() {
        v.clear();
        t=0;
        running = true;
    }

    public void addMapping(String field, String method) { addMapping(field, method, false); }
    public void addMapping(String field, String method, boolean relative) {
        try { mappings.put(field, new AnimationMapping(relative, entity.getClass().getDeclaredMethod(method, Vector.class))); }
        catch (NoSuchMethodException ignored) {}
    }

    public void addKeyframe(KeyFrame keyFrame) { keyFrames.add(keyFrame); }

    public void update(double dt) {
        if (!running) return;

        t+=dt;
        vPrev.set(v);
        int frame = (int) t;
        float decimal = (float) t - frame;
        if (frame < keyFrames.size()-1) v.mix(keyFrames.get(frame), keyFrames.get(frame+1), decimal).apply(vPrev, entity, mappings);
        else { running = false; keyFrames.getLast().apply(vPrev, entity, mappings); }
    }
}
