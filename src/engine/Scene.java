package engine;

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

    private final List<Entity>
            objects = new ArrayList<>(),
            toCreate = new ArrayList<>(),
            toDestroy = new ArrayList<>(),
            temp = new ArrayList<>();
    private Scene() {}
    @Getter private final Camera camera = new Camera();

    public void setActive() {
        activeScene = this;
        Shader.get().applyUniforms(camera);
    }

    public void update(double dt) {
        camera.update(dt);
        objects.forEach(e -> e.update(dt));

        removeObjects();
        createObjects();
    }

    public void staticUpdate(double dt) {
        objects.forEach(e -> e.staticUpdate(dt));

        removeObjects();
        createObjects();
    }

    public void render() {
        Shader.get().applyUniforms(camera);
        objects.forEach(Entity::render);
    }

    public void create(Entity entity) {
        toCreate.add(entity);
    }

    public void destroy(Entity entity) {
        toDestroy.add(entity);
    }

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

            temp.forEach(Entity::onCreate);
        }
    }
}
