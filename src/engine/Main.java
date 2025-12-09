package engine;

import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Main {
    private Main() {}

    public static void main(String[] args) {
        initialize();
        run();
        cleanup();
    }

    private static void run() {
        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, new float[]{
                -0.5f, -0.5f, 0, 1, 0, 0,
                0.5f, -0.5f, 0, 0, 1, 0,
                0, 0.5f, 0, 0, 0, 1,
                -1, 1, 0, 1, 1, 1,
                1, 1, 0, 1, 1, 1,
                1, 0, 0, 1, 1, 1}
                , GL_STATIC_DRAW);
        Shader.get().bindVBO(VBO);

        while (Window.shouldNotClose()) {
            Window.swapBuffers();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glDrawArrays(GL_TRIANGLES, 0, 6);

            InputManager.update();
            glfwPollEvents();
        }
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