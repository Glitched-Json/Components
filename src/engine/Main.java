package engine;

import game.Game;
import lombok.Getter;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.text.DecimalFormat;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {
    private static final GameLogic game = new Game();
    @Getter private static boolean running = true;

    private Main() {}

    public static void main(String[] args) {
        initialize();
        run();
        cleanup();
    }

    private static void render(int clearMask, double t) {
        Window.frameUpdate();
        glClear(clearMask);

        game.update(t);
        Scene.get().update(t);
        Scene.get().render();

        InputManager.update();
        glfwPollEvents();
    }

    private static void update(double t) {
        game.staticUpdate(t);
        Scene.get().staticUpdate(t);
    }

    private static void run() {
        DecimalFormat format = new DecimalFormat(",###");
        double targetFPS = 1d / Math.max(1e-9, DataManager.getSetting("fps"));
        double targetTPS = 1d / DataManager.getSetting("tps");
        boolean uncappedFPS = DataManager.getFlag("uncapped_FPS");

        int clearMask = GL_COLOR_BUFFER_BIT;
        if (DataManager.getFlag("depth_test")) clearMask |= GL_DEPTH_BUFFER_BIT;
        else glDisable(GL_DEPTH_TEST);

        double timeRender = 0, timeStatic = 0, tFPS = 0, elapsedTime;
        long start, end = System.nanoTime(), passedTime;
        int fpsCounter = 0, tpsCounter = 0;

        boolean showMetrics = DataManager.getFlag("show_metrics_on_window_title");

        while (Window.shouldNotClose()) {
            // Timer Updating
            start = System.nanoTime();
            passedTime = start - end;
            end = start;
            elapsedTime = passedTime / (double) 1_000_000_000L;
            timeRender += elapsedTime;
            timeStatic += elapsedTime;
            tFPS += elapsedTime;

            // Rendering
            if (Window.isVSync() || uncappedFPS) {
                render(clearMask, timeRender);
                timeRender = 0;
                fpsCounter++;
            } else if (timeRender >= targetFPS) {
                render(clearMask, timeRender);
                timeRender %= targetFPS;
                fpsCounter++;
            }

            // Static Update
            if (timeStatic >= targetTPS) {
                update(targetTPS);
                timeStatic %= targetTPS;
                tpsCounter++;
            }

            // FPS
            if (showMetrics && tFPS >= 1.) {
                tFPS %= 1.;
                Window.setTitle("FPS: %s | TPS: %s".formatted(
                        format.format(fpsCounter).replaceAll(",", "."),
                        format.format(tpsCounter).replaceAll(",", ".")
                ));
                // Window.setTitle("FPS: " + format.format(fpsCounter).replaceAll(",", "."));
                fpsCounter = tpsCounter = 0;
            }
        }
    }

    private static void initialize() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Failed to initialize GLFW.");

        Window.initialize();
        Shader.get("shader").bind();
        TextureAtlas.initialize();

        game.initialize();
    }

    private static void cleanup() {
        running = false;

        Window.cleanup();
        Shader.cleanup();
        Model.cleanup();
        TextureAtlas.cleanup();

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}