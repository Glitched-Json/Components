package engine.managers;

import engine.utils.Logic;
import lombok.Getter;

import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL30.*;

public final class SpatialManager {
    private static int fbo, color;
    private static int width, height;
    private static boolean initialized = false;
    @Getter private static boolean paused = false;
    private static final ByteBuffer buffer = createByteBuffer(3);
    @Getter private static int id = 0;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        fbo = glGenFramebuffers();

        setup("Error instantiating Spatial Manager's FrameBuffer.");
    }

    public static void searchID() {
        glPixelStorei(GL_PACK_ALIGNMENT, 1);

        int x, y;
        if (Scene.get().getCamera().isFirstPersonMode()) {
            x = width / 2;
            y = height / 2;
        } else {
            x = (int) Logic.remapClamped(0, Window.getWidth()-1, 0, width-1, InputManager.getXPosition());
            y = (int) Logic.remapClamped(0, Window.getHeight()-1, height-1, 0, InputManager.getYPosition());
        }

        glReadPixels(x, y, 1, 1, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        id = buffer.get(0) << 16 | buffer.get(1) << 8 | buffer.get(2);
    }

    public static void resize() {
        glDeleteTextures(color);
        setup("Error on Spatial Manager's FrameBuffer after Window Resize.");
    }

    private static void setup(String errorMessage) {
        width = (int) (Window.getWidth() * DataManager.getSettingClamped("spatial_detail", 0.1, 1));
        height = (int) (Window.getHeight() * DataManager.getSettingClamped("spatial_detail", 0.1, 1));

        //noinspection AssignmentUsedAsCondition
        if (paused = width == 0 || height == 0) return;

        bind();
        createTexture();
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, color, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException(errorMessage);
        unbind();
    }

    private static void createTexture() {
        glBindTexture(GL_TEXTURE_2D, color = glGenTextures());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void cleanup() {
        if (!initialized || Main.isRunning()) return;
        initialized = false;
        glDeleteTextures(color);
        glDeleteFramebuffers(fbo);
    }

    public static void bind() { glBindFramebuffer(GL_FRAMEBUFFER, fbo); }
    public static void unbind() { glBindFramebuffer(GL_FRAMEBUFFER, 0); }

    public static void setViewport() { glViewport(0, 0, width, height); }
}
