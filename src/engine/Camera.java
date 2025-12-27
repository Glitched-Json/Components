package engine;

import com.glitched.annotations.Uniform;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public final class Camera {
    public static int keyUp    = GLFW_KEY_W;
    public static int keyDown  = GLFW_KEY_S;
    public static int keyLeft  = GLFW_KEY_A;
    public static int keyRight = GLFW_KEY_D;

    @Getter private static final Vector3f position = new Vector3f(0f, 0f, 0f);
    public static final float FOV = 45, ZNear = 0.1f, ZFar = 1000f;
    @Setter private static float speed = 4f;

    public static void update(float dt) {
        if (InputManager.isDown(keyUp))    position.add(0, speed * dt, 0);
        if (InputManager.isDown(keyDown))  position.sub(0, speed * dt, 0);
        if (InputManager.isDown(keyLeft))  position.sub(speed * dt, 0, 0);
        if (InputManager.isDown(keyRight)) position.add(speed * dt, 0, 0);
    }

    @SuppressWarnings("unused")
    @Uniform("uniformView")
    public static float[] getViewMatrix() {
        return (new Matrix4f().lookAt(position, new Vector3f(position).add(0, 0, -1), new Vector3f(0, 1, 0))).get(new float[16]);
    }

    @SuppressWarnings("unused")
    @Uniform("uniformProjection")
    public static float[] getProjectionMatrix() {
        return (new Matrix4f().perspective((float)Math.toRadians(FOV), Window.getAspectRatio(), ZNear, ZFar)).get(new float[16]);
    }
}
