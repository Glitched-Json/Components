package engine;

import game.Square;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.text.DecimalFormat;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {
    private Main() {}

    public static void main(String[] args) {
        initialize();
        run();
        cleanup();
    }

    private static void run() {
        Entity entity = new Square();

        DecimalFormat format = new DecimalFormat(",###");
        double targetFPS = 1d / DataManager.getSetting("fps");
        boolean uncappedFPS = DataManager.getFlag("uncappedFPS");

        int clearMask = GL_COLOR_BUFFER_BIT;
        if (DataManager.getFlag("depthTest")) clearMask |= GL_DEPTH_BUFFER_BIT;
        else glDisable(GL_DEPTH_TEST);

        double t = 0, tFPS = 0;
        long start, end = System.nanoTime(), passedTime;
        int fpsCounter = 0;

        while (Window.shouldNotClose()) {
            start = System.nanoTime();
            passedTime = start - end;
            end = start;
            t += passedTime / (double) 1_000_000_000L;
            tFPS += passedTime / (double) 1_000_000_000L;

            if (Window.isVSync() || uncappedFPS) {
                render(clearMask, t, entity);
                t = 0;
                fpsCounter++;
            } else if (t >= targetFPS) {
                render(clearMask, t, entity);
                t %= targetFPS;
                fpsCounter++;
            }

            if (tFPS >= 1.) {
                tFPS %= 1.;
                Window.setTitle("FPS: " + format.format(fpsCounter).replaceAll(",", "."));
                fpsCounter = 0;
            }
        }
    }

    private static void render(int clearMask, double t, Entity entity) {
        Window.frameUpdate();
        glClear(clearMask);

        Camera.update(t);

        entity.render();
        entity.update(t);

        glfwPollEvents();
        InputManager.update();
    }

    private static void initialize() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Failed to initialize GLFW.");

        Window.initialize();
        Shader.get("shader").bind();
    }

    private static void cleanup() {
        Window.cleanup();
        Shader.cleanup();

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}