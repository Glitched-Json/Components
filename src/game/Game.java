package game;

import engine.GameLogic;
import engine.Scene;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Game extends GameLogic {

    public void initialize() {
        Scene.get().create(new Box(0, 0, 3));
    }

    public void update(double dt) {

    }

    public void staticUpdate(double dt) {

    }

}
