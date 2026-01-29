package engine;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2d;
import org.joml.Vector3i;
import org.joml.Vector4i;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@SuppressWarnings("unused")
public final class InputManager {
    private static final Map<Integer, Integer>
            keyInputs = new HashMap<>(),
            mouseInputs = new HashMap<>();
    @Getter private static double xPosition = 0, yPosition = 0, xOffset = 0, yOffset = 0;
    @Setter private static boolean cursorOffsetInitialized = false;

    private InputManager() {}

    public static void update() {
        for (Map.Entry<Integer, Integer> set: keyInputs.entrySet()) switch (set.getValue()) {
            case 0: set.setValue(1); break;
            case 2: set.setValue(-1); break;
        }
        for (Map.Entry<Integer, Integer> set: mouseInputs.entrySet()) switch (set.getValue()) {
            case 0: set.setValue(1); break;
            case 2: set.setValue(-1); break;
        }
    }

    // key, scancode, action, mods
    public static void registerKeyInput(Vector4i key) {
        if (key.z == GLFW_PRESS) keyInputs.put(key.x, 0);
        if (key.z == GLFW_RELEASE) keyInputs.put(key.x, 2);
    }

    // button, action, mods
    public static void registerMouseInput(Vector3i button) {
        if (button.y == GLFW_PRESS) mouseInputs.put(button.x, 0);
        if (button.y == GLFW_RELEASE) mouseInputs.put(button.x, 2);
    }

    public static void registerCursorPosition(Vector2d pos) {
        if (cursorOffsetInitialized) {
            float sensitivity = DataManager.getSetting("mouse_sensitivity");
            xOffset += (pos.x - xPosition) * sensitivity;
            yOffset += (pos.y - yPosition) * sensitivity;
        }
        cursorOffsetInitialized = true;

        xPosition = pos.x;
        yPosition = pos.y;
    }

    public static void resetOffsets() { xOffset = yOffset = 0; }

    public static boolean isPressed(int key) {return keyInputs.getOrDefault(key, -1).equals(0);}
    public static boolean isDown(int key) {return keyInputs.getOrDefault(key, -1).equals(1) || isPressed(key);}
    public static boolean isReleased(int key) {return keyInputs.getOrDefault(key, -1).equals(2);}

    public static boolean isButtonPressed(int key) {return mouseInputs.getOrDefault(key, -1).equals(0);}
    public static boolean isButtonDown(int key) {return mouseInputs.getOrDefault(key, -1).equals(1) || isButtonPressed(key);}
    public static boolean isButtonReleased(int key) {return mouseInputs.getOrDefault(key, -1).equals(2);}
}
