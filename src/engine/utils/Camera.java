package engine.utils;

import com.glitched.annotations.Uniform;
import engine.managers.DataManager;
import engine.managers.InputManager;
import engine.managers.Window;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;

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
    private final Vector3d
            position = new Vector3d(),
            front = new Vector3d(0, 0, 1),
            up = new Vector3d(0, 1, 0),
            mask = new Vector3d(1, 0, 1);
    private final Vector4d[] planes = new Vector4d[6];

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

        double scalar = getSpeed() * dt;
        if (InputManager.isDown(keyForward))   position.add(new Vector3d(front).mul(mask).normalize().mul(scalar));
        if (InputManager.isDown(keyBackward))  position.sub(new Vector3d(front).mul(mask).normalize().mul(scalar));
        if (InputManager.isDown(keyLeft))      position.sub(new Vector3d(front).cross(up).mul(mask).normalize().mul(scalar));
        if (InputManager.isDown(keyRight))     position.add(new Vector3d(front).cross(up).mul(mask).normalize().mul(scalar));
        if (InputManager.isDown(keyUp))        position.add(new Vector3d(up).mul(scalar));
        if (InputManager.isDown(keyDown))      position.sub(new Vector3d(up).mul(scalar));
        if (InputManager.isPressed(keySprint)) sprinting = !sprinting;
    }

    public void staticUpdate() {
        calculateFrustum();
    }

    public double getSpeed() {
        return sprinting ? 2d * speed : speed;
    }

    private void calculateFrustum() {
        Vector3d X = new Vector3d(front).cross(up).normalize();
        Vector3d Y = new Vector3d(X).cross(front).normalize();

        float nearH = 2f * (float) Math.tan(Math.toRadians(FOV) / 2) * ZNear;
        float nearW = nearH * Window.getAspectRatio();

        Vector3d nearCenter = new Vector3d(front).mul(ZNear);
        Vector3d farCenter = new Vector3d(front).mul(ZFar);

        Vector3d ntl = new Vector3d(nearCenter).add(new Vector3d(Y).mul(nearH/2.)).sub(new Vector3d(X).mul(nearW/2.));
        Vector3d ntr = new Vector3d(nearCenter).add(new Vector3d(Y).mul(nearH/2.)).add(new Vector3d(X).mul(nearW/2.));
        Vector3d nbl = new Vector3d(nearCenter).sub(new Vector3d(Y).mul(nearH/2.)).sub(new Vector3d(X).mul(nearW/2.));
        Vector3d nbr = new Vector3d(nearCenter).sub(new Vector3d(Y).mul(nearH/2.)).add(new Vector3d(X).mul(nearW/2.));
        nearCenter.add(position);
        farCenter.add(position);

        planes[0] = getPlane(new Vector3d(nbl).cross(ntl).normalize(), position); // Left
        planes[1] = getPlane(new Vector3d(ntr).cross(nbr).normalize(), position); // Right
        planes[2] = getPlane(new Vector3d(ntl).cross(ntr).normalize(), position); // Top
        planes[3] = getPlane(new Vector3d(nbr).cross(nbl).normalize(), position); // Bottom
        planes[4] = getPlane(front, nearCenter);                                  // Near
        planes[5] = getPlane(new Vector3d(front).mul(-1), farCenter);       // Far
    }

    private Vector4d getPlane(Vector3d normal, Vector3d point) {
        return new Vector4d(normal, -normal.dot(point));
    }

    public boolean isInView(Vector3f min, Vector3f max) {
        return !(checkPlane(planes[0], min, max)
                || checkPlane(planes[1], min, max)
                || checkPlane(planes[2], min, max)
                || checkPlane(planes[3], min, max)
                || checkPlane(planes[4], min, max)
                || checkPlane(planes[5], min, max));
    }

    private boolean checkPlane(Vector4d plane, Vector3f min, Vector3f max) {
        Vector4d p = new Vector4d(
                plane.x > 0 ? max.x : min.x,
                plane.y > 0 ? max.y : min.y,
                plane.z > 0 ? max.z : min.z,
                0
        );
        return plane.dot(p) + plane.w < 0;
    }

    @SuppressWarnings("unused")
    @Uniform("view")
    public float[] getViewMatrix() {
        return new Matrix4f().lookAt(new Vector3f(position), new Vector3f(position).add(new Vector3f(front)), new Vector3f(up)).get(new float[16]);
    }

    @SuppressWarnings("unused")
    @Uniform("projection")
    public float[] getProjectionMatrix() {
        return new Matrix4f().perspective((float) toRadians(FOV), Window.getAspectRatio(), ZNear, ZFar).get(new float[16]);
    }
}
