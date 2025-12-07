package engine;

import org.lwjgl.glfw.GLFWErrorCallback;

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
        while (Window.shouldNotClose()) {
            Window.swapBuffers();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            InputManager.update();
            glfwPollEvents();
        }
    }

    private static void initialize() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Failed to initialize GLFW.");

        Window.initialize();
    }

    private static void cleanup() {
        Window.cleanup();

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}