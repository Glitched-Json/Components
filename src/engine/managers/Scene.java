package engine.managers;

import engine.utils.Camera;
import engine.utils.Entity;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class Scene {
    private static final Map<String, Scene> scenes = new HashMap<>();
    private static Scene activeScene = get("main");

    @NotNull public static Scene get() { return activeScene; }

    public static Scene get(String scene) {
        if (scenes.containsKey(scene)) return scenes.get(scene);
        Scene s = new Scene();
        scenes.put(scene, s);
        return s;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private int lastID = 1;
    private final List<Entity>
            objects = new ArrayList<>(),
            toCreate = new ArrayList<>(),
            toDestroy = new ArrayList<>(),
            temp = new ArrayList<>();
    @Getter private final Camera camera = new Camera();

    private Scene() {}

    public void setActive() {
        activeScene = this;
        Shader.get().applyUniforms(camera);
    }

    public void update(double dt) {
        camera.update(dt);
        objects.parallelStream().forEach(e -> {
            e.update(dt);
            e.updateAnimation(dt);
        });

        removeObjects();
        createObjects();
    }

    public void staticUpdate(double dt) {
        camera.staticUpdate();
        objects.parallelStream().forEach(e -> {
            e.checkVisibility();
            e.staticUpdate(dt);
        });

        removeObjects();
        createObjects();
    }

    public void renderSpatial() {
        Shader.get("spatial_shader").bind();
        Shader.get().applyUniforms(camera);
        objects.stream()
                .filter(Entity::isVisible)
                .sorted((a, b) -> Double.compare(b.getPosition().z, a.getPosition().z))
                .forEach(Entity::spatialRender);
    }

    public void render() {
        Shader.get().applyUniforms(camera);
        objects.stream()
                .filter(Entity::isVisible)
                .sorted((a, b) -> Double.compare(b.getPosition().z, a.getPosition().z))
                .forEach(Entity::render);
    }

    public void create(Entity entity) {
        toCreate.add(entity);
    }

    public void destroy(Entity entity) {
        toDestroy.add(entity);
    }

    public List<Entity> getObjects() { return new ArrayList<>(objects); }

    private void removeObjects() {
        while (!toDestroy.isEmpty()) {
            toCreate.removeAll(toDestroy);
            objects.removeAll(toDestroy);

            temp.clear();
            temp.addAll(toDestroy);
            toDestroy.clear();

            temp.forEach(Entity::onDestroy);
        }
    }

    private void createObjects() {
        while (!toCreate.isEmpty()) {
            objects.addAll(toCreate);

            temp.clear();
            temp.addAll(toCreate);
            toCreate.clear();

            temp.forEach(e -> {
                e.setID(lastID++);
                e.onCreate();
            });
        }
    }
}
