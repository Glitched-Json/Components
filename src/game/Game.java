package game;

import engine.managers.Scene;
import engine.utils.GameLogic;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Game extends GameLogic {

    public void initialize() {
        Scene.get().create(new Square(0, 0, 3));
        Scene.get().create(new Sprite(0, 0, 2.9));
    }

    public void update(double dt) {

    }

    public void staticUpdate(double dt) {

    }

}
