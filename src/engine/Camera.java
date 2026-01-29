package engine;

import com.glitched.annotations.Uniform;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;

@Getter
public final class Camera {
    public int keyForward  = GLFW_KEY_W;
    public int keyBackward = GLFW_KEY_S;
    public int keyLeft     = GLFW_KEY_A;
    public int keyRight    = GLFW_KEY_D;
    public int keyUp       = GLFW_KEY_SPACE;
    public int keyDown     = GLFW_KEY_LEFT_SHIFT;
    public int keySprint   = GLFW_KEY_LEFT_CONTROL;

    private boolean firstPersonMode;
    @Setter private float speed = 4f;
    private float yaw = 90, pitch = 0;
    private boolean sprinting = false;
    @Setter public float FOV = 45, ZNear = 0.1f, ZFar = 1000f;
    private final Vector3f
            position = new Vector3f(),
            front = new Vector3f(0, 0, 1),
            up = new Vector3f(0, 1, 0),
            mask = new Vector3f(1, 0, 1);

    public Camera() {
        setFirstPersonMode(DataManager.getFlag("first_person_mode"));
    }

    public void setFirstPersonMode(boolean firstPersonMode) {
        //noinspection AssignmentUsedAsCondition
        if (this.firstPersonMode = firstPersonMode) {
            keyForward  = GLFW_KEY_W;
            keyBackward = GLFW_KEY_S;
            keyLeft     = GLFW_KEY_A;
            keyRight    = GLFW_KEY_D;
            keyUp       = GLFW_KEY_SPACE;
            keyDown     = GLFW_KEY_LEFT_SHIFT;
            keySprint   = GLFW_KEY_LEFT_CONTROL;

            InputManager.resetOffsets();
            InputManager.setCursorOffsetInitialized(false);
            mask.set(1, 0, 1);
        } else {
            keyForward  = GLFW_KEY_UNKNOWN;
            keyBackward = GLFW_KEY_UNKNOWN;
            keyLeft     = GLFW_KEY_A;
            keyRight    = GLFW_KEY_D;
            keyUp       = GLFW_KEY_W;
            keyDown     = GLFW_KEY_S;
            keySprint   = GLFW_KEY_LEFT_SHIFT;
            mask.set(1);
        }
        Window.lockCursor(firstPersonMode);
    }

    public void update(double dt) {
        if (firstPersonMode) {
            yaw = (yaw + (float) InputManager.getXOffset()) % 360;
            pitch = Logic.clamp(pitch - (float) InputManager.getYOffset(), -89.99f, 89.99f);
            InputManager.resetOffsets();
            front.set(
                    cos(toRadians(yaw)) * cos(toRadians(pitch)),
                    sin(toRadians(pitch)),
                    sin(toRadians(yaw)) * cos(toRadians(pitch))
            );
        }

        float t = (float) dt;
        if (InputManager.isDown(keyForward))   position.add(new Vector3f(front).mul(mask).normalize().mul(getSpeed() * t));
        if (InputManager.isDown(keyBackward))  position.sub(new Vector3f(front).mul(mask).normalize().mul(getSpeed() * t));
        if (InputManager.isDown(keyLeft))      position.sub(new Vector3f(front).cross(up).mul(mask).normalize().mul(getSpeed() * t));
        if (InputManager.isDown(keyRight))     position.add(new Vector3f(front).cross(up).mul(mask).normalize().mul(getSpeed() * t));
        if (InputManager.isDown(keyUp))        position.add(new Vector3f(up).mul(getSpeed() * t));
        if (InputManager.isDown(keyDown))      position.sub(new Vector3f(up).mul(getSpeed() * t));
        if (InputManager.isPressed(keySprint)) sprinting = !sprinting;
    }

    public float getSpeed() {
        return sprinting ? 2 * speed : speed;
    }

    @SuppressWarnings("unused")
    @Uniform("view")
    public float[] getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up).get(new float[16]);
    }

    @SuppressWarnings("unused")
    @Uniform("projection")
    public float[] getProjectionMatrix() {
        return new Matrix4f().perspective((float) toRadians(FOV), Window.getAspectRatio(), ZNear, ZFar).get(new float[16]);
    }
}
