package engine.utils;

import engine.managers.Scene;
import engine.managers.Window;

import java.util.List;

@SuppressWarnings("unused")
public abstract class GameLogic {
    protected GameLogic() {}

    public abstract void initialize();
    public abstract void update(double dt);
    public abstract void staticUpdate(double dt);

    protected void create(Entity entity) { Scene.get().create(entity); }
    protected void destroy(Entity entity) { Scene.get().destroy(entity); }
    protected List<Entity> loadedEntities() { return Scene.get().getObjects(); }

    protected void setTitle(String title) { Window.setTitle(title); }
}
