package engine.utils;

public abstract class GameLogic {
    protected GameLogic() {}

    public abstract void initialize();
    public abstract void update(double dt);
    public abstract void staticUpdate(double dt);
}
