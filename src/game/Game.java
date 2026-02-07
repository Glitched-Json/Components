package game;

import engine.managers.Scene;
import engine.utils.GameLogic;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Game extends GameLogic {

    public void initialize() {
        for (int i=0; i<360; i+=20)
            Scene.get().create(new Circle(
                    Math.cos(Math.toRadians(i))*2,
                    Math.sin(Math.toRadians(i))*2,
                    3
                    , -Math.cos(Math.toRadians(i)), -Math.sin(Math.toRadians(i)), 0
            ));
    }

    public void update(double dt) {

    }

    public void staticUpdate(double dt) {

    }

}
