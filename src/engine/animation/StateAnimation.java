package engine.animation;

import engine.utils.Entity;
import engine.utils.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class StateAnimation {
    private final Entity entity;
    private final Map<String, AnimationMapping> mappings = new HashMap<>(){
        private void addMapping(String key, String method, boolean relative) {
            try { put(key, new AnimationMapping(relative, Entity.class.getDeclaredMethod(method, Vector.class))); }
            catch (NoSuchMethodException ignored) {}
        }

        {
            addMapping("pos", "move", true);
            addMapping("rotation", "rotate", true);
            addMapping("size", "setScale", false);
        }
    };

    public StateAnimation(Entity entity) {
        this.entity = entity;
    }

    private final KeyFrame v = new KeyFrame(), vPrev = new KeyFrame();
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private double t, tPrev;
    private int frame;
    private boolean running = false;
    public boolean start() { return start(false); }
    public boolean start(boolean runImmediately) {
        if (running && !runImmediately) return false;
        v.clear();
        vPrev.clear();
        t = tPrev = frame = 0;
        running = true;
        return true;
    }

    public void addMapping(String field, String method) { addMapping(field, method, false); }
    public void addMapping(String field, String method, boolean relative) {
        try { mappings.put(field, new AnimationMapping(relative, entity.getClass().getDeclaredMethod(method, Vector.class))); }
        catch (NoSuchMethodException ignored) {}
    }

    public void addKeyframe(KeyFrame keyFrame) { keyFrames.add(keyFrame); }

    public void update(double dt) {
        if (!running) return;

        if (t-tPrev > keyFrames.get(frame+1).getTime()) {
            tPrev += keyFrames.get(frame+1).getTime();
            vPrev.set(v);
            v.mix(keyFrames.get(frame), keyFrames.get(frame+1), keyFrames.get(frame+1).getTime()).apply(vPrev, entity, mappings);
            if (++frame >= keyFrames.size()-1) {
                running = false;
                vPrev.set(v);
                keyFrames.getLast().apply(vPrev, entity, mappings);
            }
            return;
        }

        vPrev.set(v);
        v.mix(keyFrames.get(frame), keyFrames.get(frame+1), t-tPrev).apply(vPrev, entity, mappings);
        t+=dt;
    }
}
