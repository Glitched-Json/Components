package game;

import engine.utils.GameLogic;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Game extends GameLogic {

    public void initialize() {
        create(new Circle(0, 0, 3));
    }

    public void update(double dt) {

    }

    public void staticUpdate(double dt) {

    }

}
